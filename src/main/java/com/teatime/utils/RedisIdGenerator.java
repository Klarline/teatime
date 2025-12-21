package com.teatime.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisIdGenerator {

  private static final long BEGIN_TIMESTAMP = 1735689600L;
  private static final int COUNT_BITS = 32;

  private StringRedisTemplate stringRedisTemplate;

  public RedisIdGenerator(StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
  }

  public long nextId(String keyPrefix) {
    // generate timestamp
    LocalDateTime now = LocalDateTime.now();
    long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
    long timestamp = nowSecond - BEGIN_TIMESTAMP;

    // generate sequence number
    String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
    Long count = stringRedisTemplate.opsForValue().increment("icr" + keyPrefix + date);

    return (timestamp << COUNT_BITS) | count;
  }
}
