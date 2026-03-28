package com.maxin.utils.impl;

import cn.hutool.core.lang.UUID;
import com.maxin.utils.RedisLock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class RedisLockImpl implements RedisLock {

    private String name;
    private RedisTemplate redisTemplate;

    public RedisLockImpl(String name, RedisTemplate redisTemplate) {
        this.name = name;
        this.redisTemplate = redisTemplate;
    }

    private static final String KEY_PREFIX = "lock:";
    private static final String ID_PREFIX = UUID.randomUUID().toString(true);
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;
    static {
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("unlock.lua"));
        UNLOCK_SCRIPT.setResultType(Long.class);
    }

    /**
     * 尝试获取锁
     * @param timeoutSec 锁持有的超时时间，过期后自动释放
     * @return true代表获取锁成功，false代表获取锁失败
     */
    public boolean tryLock(long timeoutSec) {
        // 获取线程标示
        String threadId = ID_PREFIX + Thread.currentThread().getId();
        // 获取锁
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(KEY_PREFIX + name, threadId, timeoutSec, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    /**
     * 释放锁
     */
    public void unlock() {
        // 调用lua脚本
        String threadId = ID_PREFIX + Thread.currentThread().getId();
        redisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(ID_PREFIX + name), threadId);
    }
    /*public void unlock() {
        // 获取线程表示和锁终标示
        String threadId = ID_PREFIX + Thread.currentThread().getId();
        String id = stringRedisTemplate.opsForValue().get(KEY_PREFIX + name);

        if (threadId.equals(id)) {
            stringRedisTemplate.delete(KEY_PREFIX + name);
        }
    }*/
}
