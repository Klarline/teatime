package com.teatime.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisIdGenerator {

  private static final long BEGIN_TIMESTAMP = 1735689600L;
  private static final int COUNT_BITS = 32;

  private final StringRedisTemplate stringRedisTemplate;
  private final AtomicLong fallbackCounter = new AtomicLong(0);

  public RedisIdGenerator(StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
  }

  public long nextId(String keyPrefix) {
    LocalDateTime now = LocalDateTime.now();
    long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
    long timestamp = nowSecond - BEGIN_TIMESTAMP;

    Long count = RedisFallback.execute(
        () -> {
          String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
          return stringRedisTemplate.opsForValue().increment("icr" + keyPrefix + date);
        },
        () -> fallbackCounter.incrementAndGet()
    );

    return (timestamp << COUNT_BITS) | (count != null ? count : 0);
  }
}
