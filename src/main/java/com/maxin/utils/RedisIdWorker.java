package com.maxin.utils;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class RedisIdWorker {

    // 开始时间戳，2022-1-1 00:00
    private static final long BEGIN_TIMESTAMP = 1640995200L;
    // 序列号位数
    private static final int COUNT_BITS = 32;

    private RedisTemplate redisTemplate;

    public RedisIdWorker(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public long nextId(String keyPrefix) {
        // 生成时间戳
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timeStamp = nowSecond - BEGIN_TIMESTAMP;

        // 生成序列号
        String date = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Long count = redisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);

        // 拼接，生成id
        return timeStamp << COUNT_BITS | count;
    }

}
