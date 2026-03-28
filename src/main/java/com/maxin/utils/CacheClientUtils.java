package com.maxin.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.maxin.constant.RedisConstant;
import com.maxin.result.Result;
import com.maxin.entity.Shop;
import com.maxin.entity.RedisData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Component
@Slf4j
public class CacheClientUtils {

    private RedisTemplate redisTemplate;

    public CacheClientUtils(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void set(String key, Object value, Long time, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
    }

    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit) {
        // 设置逻辑对象
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));

        redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    public <R, ID> R queryWithPassThrough(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        String json = redisTemplate.opsForValue().get(key).toString();
        if (StrUtil.isNotBlank(json)) {
            return JSONUtil.toBean(json, type);
        }
        if (json != null) {
            return null;
        }

        R r = dbFallback.apply(id);
        if (r == null) {
            redisTemplate.opsForValue().set(key, "", RedisConstant.CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }
        this.set(key, r, time, unit);

        return r;
    }

    private <R, ID> R queryWithMutex(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        String json = redisTemplate.opsForValue().get(key).toString();
        if (StrUtil.isNotBlank(json)) {
           return JSONUtil.toBean(json, type);
        }
        if (json != null) {
            return null;
        }

        String lockKey = RedisConstant.LOCK_SHOP_KEY + id;
        R r = null;
        try {
            boolean isLock = tryLock(lockKey);
            // 栈溢出风险！！！考虑到是在学Redis这里就不关注了
            if (!isLock) {
                Thread.sleep(50);
                return queryWithMutex(keyPrefix, id, type, dbFallback, time, unit);
            }

            r = dbFallback.apply(id);
            if (r == null) {
                this.set(key, "", time, unit);
                return null;
            }

            this.set(key, r, time, unit);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            unLock(lockKey);
        }

        return r;
    }

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public <R, ID> R queryWithLogicalExpire(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = RedisConstant.CACHE_SHOP_KEY + id;
        String json = redisTemplate.opsForValue().get(key).toString();
        if (StrUtil.isBlank(json)) {
            return null;
        }

        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        R r = JSONUtil.toBean((JSONObject) redisData.getData(), type);
        LocalDateTime expireTime = redisData.getExpireTime();

        // 未过期，直接返回商店信息
        if (expireTime.isAfter(LocalDateTime.now())) {
            return r;
        }

        // 已过期，需要缓存重建
        String lockKey = RedisConstant.LOCK_SHOP_KEY + id;
        boolean isLock = tryLock(lockKey);
        if (isLock) {
            // 开启独立线程，实施缓存重建
            CACHE_REBUILD_EXECUTOR.execute(() -> {
                try {
                    R r1 = dbFallback.apply(id);
                    this.setWithLogicalExpire(keyPrefix + id, r1, time, unit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    unLock(lockKey);
                }
            });
        }

        return r;
    }

    private boolean tryLock(String key) {
        Boolean flag = redisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    private void unLock(String key) {
        redisTemplate.delete(key);
    }
}
