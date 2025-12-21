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
   */
  public <R, ID> R query(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallBack,
                         Long time, TimeUnit unit) {
    String key = keyPrefix + id;
    String json = stringRedisTemplate.opsForValue().get(key);

    if (StrUtil.isNotBlank(json)) {
      return JSONUtil.toBean(json, type);
    }

    R r = dbFallBack.apply(id);
    if (r == null) {
      return null;
    }
    this.set(key, r, time, unit);
    return r;
  }

  /**
   * Query with cache pass-through (cache penetration protection)
   * Use for: Data that might not exist, susceptible to malicious queries
   */
  public <R, ID> R queryWithPassThrough(String keyPrefix, ID id, Class<R> type,
                                        Function<ID, R> dbFallBack, Long time, TimeUnit unit) {
    String key = keyPrefix + id;

    // check Redis cache
    String json = stringRedisTemplate.opsForValue().get(key);
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
      stringRedisTemplate.opsForValue()
          .set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
      return null;
    }

    // store information in Redis
    this.set(key, r, time, unit);

    return r;
  }

  /**
   * Query with mutex lock (cache breakdown protection)
   * Use for: Hot keys where data consistency is critical (inventory, pricing)
   */
  public <R, ID> R queryWithMutex(String keyPrefix, ID id, Class<R> type,
                                  Function<ID, R> dbFallBack, Long time, TimeUnit unit) {
    String key = keyPrefix + id;

    // check Redis cache for information
    String json = stringRedisTemplate.opsForValue().get(key);
    if (StrUtil.isNotBlank(json)) {
      return JSONUtil.toBean(json, type);
    }

    if (json != null) {
      // cached null value indicates json does not exist
      return null;
    }

    // implement mutex lock to prevent cache breakdown
    String lockKey = "lock:" + key;
    R r = null;
    try {
      boolean isLock = tryLock(lockKey);
      if (isLock) {
        // did not get the lock, sleep and retry
        Thread.sleep(50);
        return queryWithMutex(keyPrefix, id, type, dbFallBack, time, unit);
      }

      // Got lock - double check cache (maybe another thread already rebuilt it)
      json = stringRedisTemplate.opsForValue().get(key);
      if (StrUtil.isNotBlank(json)) {
        return JSONUtil.toBean(json, type);
      }

      // if got the lock, query database
      r = dbFallBack.apply(id);
      if (r == null) {
        // cache null value to prevent cache penetration
        stringRedisTemplate.opsForValue()
            .set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
        return null;
      }

      // store information in Redis
      this.set(key, r, time, unit);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } finally {
      // release lock
      unlock(lockKey);
    }
    return r;
  }

  /**
   * Query with logical expiration (cache breakdown protection)
   * Use for: Hot keys where availability is critical and stale data is acceptable
   */
  public <R, ID> R queryWithLogicalExpire(String keyPrefix, ID id, Class<R> type,
                                          Function<ID, R> dbFallBack, Long time, TimeUnit unit) {
    String key = keyPrefix + id;

    // check Redis cache for information
    String json = stringRedisTemplate.opsForValue().get(key);
    if (StrUtil.isBlank(json)) {
      return null;
    }

    // if found in cache, deserialize RedisData
    RedisData redisData = JSONUtil.toBean(json, RedisData.class);
    R r = JSONUtil.toBean((JSONObject) (redisData.getData()), type);
    LocalDateTime expireTime = redisData.getExpireTime();

    if (expireTime.isAfter(LocalDateTime.now())) {
      // not expired
      return r;
    }

    String lockKey = "lock:" + key;
    boolean isLock = tryLock(lockKey);
    if (isLock) {
      CACHE_REBUILD_EXECUTOR.submit(() -> {
        try {
          R r1 = dbFallBack.apply(id);
          // rebuild cache
          this.setLogicalExpire(key, r1, time, unit);
        } catch (Exception e) {
          throw new RuntimeException(e);
        } finally {
          // release lock
          unlock(lockKey);
        }
      });
    }
    return r;
  }

  // Helper methods
  public void set(String key, Object value, Long time, TimeUnit unit) {
    stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
  }

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