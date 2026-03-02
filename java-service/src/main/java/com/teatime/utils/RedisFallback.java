package com.teatime.utils;

import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;

/**
 * Utility for executing Redis operations with graceful fallback when Redis is unavailable.
 * Catches DataAccessException (including RedisConnectionFailureException) and executes
 * fallback logic instead of propagating the error.
 */
@Slf4j
public final class RedisFallback {

  private RedisFallback() {
  }

  /**
   * Execute a Redis operation with fallback when Redis is unavailable.
   *
   * @param redisOp   The Redis operation to execute
   * @param fallback  The fallback to execute when Redis fails
   * @return Result from Redis operation, or fallback result on failure
   */
  public static <T> T execute(Supplier<T> redisOp, Supplier<T> fallback) {
    try {
      return redisOp.get();
    } catch (DataAccessException e) {
      log.warn("Redis unavailable, using fallback: {}", e.getMessage());
      return fallback.get();
    }
  }

  /**
   * Execute a Redis operation that returns void. On failure, the operation is skipped
   * (best-effort semantics).
   *
   * @param redisOp The Redis operation to execute
   */
  public static void executeVoid(Runnable redisOp) {
    try {
      redisOp.run();
    } catch (DataAccessException e) {
      log.warn("Redis unavailable, skipping operation: {}", e.getMessage());
    }
  }

  /**
   * Execute a Redis operation with fallback when Redis is unavailable.
   * For operations that may throw, use this to catch and run fallback.
   *
   * @param redisOp   The Redis operation to execute
   * @param fallback  The fallback to execute when Redis fails
   * @return true if Redis succeeded, false if fallback was used
   */
  public static boolean executeWithStatus(Runnable redisOp, Runnable fallback) {
    try {
      redisOp.run();
      return true;
    } catch (DataAccessException e) {
      log.warn("Redis unavailable, using fallback: {}", e.getMessage());
      fallback.run();
      return false;
    }
  }
}
