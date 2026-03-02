package com.teatime.utils;

import static com.teatime.utils.RedisConstants.CACHE_NULL_TTL;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class CacheClient {
  private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);
  private final StringRedisTemplate stringRedisTemplate;

  public CacheClient(StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
  }

  /**
   * Basic cache query method
   * Use for: Non-critical data, low traffic scenarios
   * Falls back to DB when Redis is unavailable.
   *
   * @param keyPrefix  Redis key prefix
   * @param id         Unique identifier for the data
   * @param type       Class type of the return value
   * @param dbFallBack Function to query the database if cache miss
   * @param time       Cache expiration time
   * @param unit       Time unit for expiration time
   * @return R          The queried data
   */
  public <R, ID> R query(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallBack,
                         Long time, TimeUnit unit) {
    String key = keyPrefix + id;
    String json = RedisFallback.execute(
        () -> stringRedisTemplate.opsForValue().get(key),
        () -> null
    );

    if (StrUtil.isNotBlank(json)) {
      return JSONUtil.toBean(json, type);
    }

    R r = dbFallBack.apply(id);
    if (r == null) {
      return null;
    }
    RedisFallback.executeVoid(() -> this.set(key, r, time, unit));
    return r;
  }

  /**
   * Query with cache pass-through (cache penetration protection)
   * Use for: Data that might not exist, susceptible to malicious queries
   *
   * @param keyPrefix  Redis key prefix
   * @param id         Unique identifier for the data
   * @param type       Class type of the return value
   * @param dbFallBack Function to query the database if cache miss
   * @param time       Cache expiration time
   * @param unit       Time unit for expiration time
   * @return R          The queried data
   */
  public <R, ID> R queryWithPassThrough(String keyPrefix, ID id, Class<R> type,
                                        Function<ID, R> dbFallBack, Long time, TimeUnit unit) {
    String key = keyPrefix + id;

    // check Redis cache (falls back to null on Redis outage)
    String json = RedisFallback.execute(
        () -> stringRedisTemplate.opsForValue().get(key),
        () -> null
    );
    if (StrUtil.isNotBlank(json)) {
      return JSONUtil.toBean(json, type);
    }

    if (json != null) {
      // cached null value indicates json does not exist
      return null;
    }

    // if not found in cache, query database
    R r = dbFallBack.apply(id);
    if (r == null) {
      // cache null value to prevent cache penetration
      RedisFallback.executeVoid(() -> stringRedisTemplate.opsForValue()
          .set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES));
      return null;
    }

    // store information in Redis
    RedisFallback.executeVoid(() -> this.set(key, r, time, unit));

    return r;
  }

  /**
   * Query with mutex lock (cache breakdown protection)
   * Use for: Hot keys where data consistency is critical (inventory, pricing)
   *
   * @param keyPrefix  Redis key prefix
   * @param id         Unique identifier for the data
   * @param type       Class type of the return value
   * @param dbFallBack Function to query the database if cache miss
   * @param time       Cache expiration time
   * @param unit       Time unit for expiration time
   * @return R          The queried data
   */
  public <R, ID> R queryWithMutex(String keyPrefix, ID id, Class<R> type,
                                  Function<ID, R> dbFallBack, Long time, TimeUnit unit) {
    String key = keyPrefix + id;

    // check Redis cache for information (falls back to null on Redis outage)
    String json = RedisFallback.execute(
        () -> stringRedisTemplate.opsForValue().get(key),
        () -> null
    );
    if (StrUtil.isNotBlank(json)) {
      return JSONUtil.toBean(json, type);
    }

    if (json != null) {
      // cached null value indicates json does not exist
      return null;
    }

    // When Redis is down, skip mutex and go directly to DB (degraded but functional)
    Boolean lockResult = RedisFallback.execute(() -> tryLock("lock:" + key), () -> false);
    boolean isLock = BooleanUtil.isTrue(lockResult);

    if (!isLock) {
      // Redis down or lock contention - fallback: query DB directly without cache
      R r = dbFallBack.apply(id);
      if (r != null) {
        RedisFallback.executeVoid(() -> this.set(key, r, time, unit));
      } else {
        RedisFallback.executeVoid(() -> stringRedisTemplate.opsForValue()
            .set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES));
      }
      return r;
    }

    // Got the lock - proceed with cache rebuild
    R r = null;
    try {
      // Got lock - double check cache (maybe another thread already rebuilt it)
      json = RedisFallback.execute(
          () -> stringRedisTemplate.opsForValue().get(key),
          () -> null
      );
      if (StrUtil.isNotBlank(json)) {
        return JSONUtil.toBean(json, type);
      }

      // if got the lock, query database
      r = dbFallBack.apply(id);
      if (r == null) {
        // cache null value to prevent cache penetration
        RedisFallback.executeVoid(() -> stringRedisTemplate.opsForValue()
            .set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES));
        return null;
      }

      // store information in Redis
      final R resultToCache = r;
      RedisFallback.executeVoid(() -> this.set(key, resultToCache, time, unit));
    } finally {
      // release lock (only executed when we have the lock)
      RedisFallback.executeVoid(() -> unlock("lock:" + key));
    }
    return r;
  }

  /**
   * Query with logical expiration (cache breakdown protection)
   * If cache is empty, queries database and sets up cache
   * Use for: Hot keys where availability is critical and stale data is acceptable
   *
   * @param keyPrefix  Redis key prefix
   * @param id         Unique identifier for the data
   * @param type       Class type of the return value
   * @param dbFallBack Function to query the database if cache miss
   * @param time       Cache expiration time
   * @param unit       Time unit for expiration time
   * @return R          The queried data
   */
  public <R, ID> R queryWithLogicalExpire(String keyPrefix, ID id, Class<R> type,
                                          Function<ID, R> dbFallBack, Long time, TimeUnit unit) {
    String key = keyPrefix + id;

    // Check Redis cache for information (falls back to null on Redis outage)
    String json = RedisFallback.execute(
        () -> stringRedisTemplate.opsForValue().get(key),
        () -> null
    );

    // Handle cold start (empty cache) or Redis outage
    if (StrUtil.isBlank(json)) {
      // Query database
      R r = dbFallBack.apply(id);

      // If data doesn't exist in database, return null
      if (r == null) {
        return null;
      }

      // If data exists, set up cache with logical expiration
      RedisFallback.executeVoid(() -> this.setLogicalExpire(key, r, time, unit));
      return r;
    }

    // Cache hit - deserialize RedisData
    RedisData redisData = JSONUtil.toBean(json, RedisData.class);
    R r = JSONUtil.toBean((JSONObject) (redisData.getData()), type);
    LocalDateTime expireTime = redisData.getExpireTime();

    if (expireTime.isAfter(LocalDateTime.now())) {
      // not expired
      return r;
    }

    // Cache expired - return stale data and refresh asynchronously
    String lockKey = "lock:" + key;
    Boolean lockResult = RedisFallback.execute(() -> tryLock(lockKey), () -> false);
    boolean isLock = BooleanUtil.isTrue(lockResult);
    if (isLock) {
      CACHE_REBUILD_EXECUTOR.submit(() -> {
        try {
          R r1 = dbFallBack.apply(id);
          // rebuild cache
          RedisFallback.executeVoid(() -> this.setLogicalExpire(key, r1, time, unit));
        } finally {
          // release lock
          RedisFallback.executeVoid(() -> unlock(lockKey));
        }
      });
    }
    return r;
  }

  // Helper methods

  /**
   * Set data in Redis with expiration
   *
   * @param key   Redis key
   * @param value Data to store
   * @param time  Expiration time
   * @param unit  Time unit for expiration time
   */
  public void set(String key, Object value, Long time, TimeUnit unit) {
    stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
  }

  /**
   * Set data with logical expiration
   *
   * @param key   Redis key
   * @param value Data to store
   * @param time  Expiration time
   * @param unit  Time unit for expiration time
   */
  public void setLogicalExpire(String key, Object value, Long time, TimeUnit unit) {
    // create RedisData object with logical expiration
    RedisData redisData = new RedisData();
    redisData.setData(value);
    redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));

    // store in Redis
    stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
  }

  private boolean tryLock(String key) {
    Boolean flag = stringRedisTemplate.opsForValue()
        .setIfAbsent(key, "1", 10L, TimeUnit.SECONDS);
    return BooleanUtil.isTrue(flag);
  }

  private void unlock(String key) {
    stringRedisTemplate.delete(key);
  }
}
