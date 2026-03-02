package com.teatime.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;

class RedisFallbackTest {

  @Test
  void execute_RedisSucceeds_ReturnsRedisResult() {
    String result = RedisFallback.execute(() -> "from-redis", () -> "from-fallback");
    assertEquals("from-redis", result);
  }

  @Test
  void execute_RedisThrowsDataAccessException_ReturnsFallbackResult() {
    DataAccessException exception = new DataAccessException("Connection refused") {};
    String result = RedisFallback.execute(
        () -> {
          throw exception;
        },
        () -> "from-fallback"
    );
    assertEquals("from-fallback", result);
  }

  @Test
  void execute_RedisReturnsNull_FallbackNotUsed() {
    String result = RedisFallback.execute(() -> null, () -> "fallback");
    assertNull(result);
  }

  @Test
  void executeVoid_RedisSucceeds_CompletesNormally() {
    boolean[] executed = {false};
    assertDoesNotThrow(() ->
        RedisFallback.executeVoid(() -> executed[0] = true));
    assertTrue(executed[0]);
  }

  @Test
  void executeVoid_RedisThrowsDataAccessException_DoesNotPropagate() {
    DataAccessException exception = new DataAccessException("Connection refused") {};
    assertDoesNotThrow(() ->
        RedisFallback.executeVoid(() -> {
          throw exception;
        }));
  }

  @Test
  void executeWithStatus_RedisSucceeds_ReturnsTrue() {
    boolean result = RedisFallback.executeWithStatus(
        () -> {},
        () -> fail("Fallback should not run")
    );
    assertTrue(result);
  }

  @Test
  void executeWithStatus_RedisThrows_RunsFallbackAndReturnsFalse() {
    boolean[] fallbackRan = {false};
    DataAccessException exception = new DataAccessException("Connection refused") {};

    boolean result = RedisFallback.executeWithStatus(
        () -> {
          throw exception;
        },
        () -> fallbackRan[0] = true
    );

    assertFalse(result);
    assertTrue(fallbackRan[0]);
  }
}
