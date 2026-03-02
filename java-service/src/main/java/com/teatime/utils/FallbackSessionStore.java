package com.teatime.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * In-memory fallback store for login codes and session tokens when Redis is unavailable.
 * Used only during Redis outage - data is not persistent and is lost on restart.
 */
@Slf4j
public final class FallbackSessionStore {

  private static final ConcurrentHashMap<String, String> LOGIN_CODES = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<String, Long> CODE_EXPIRY = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<String, Map<Object, Object>> SESSIONS =
      new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<String, Long> SESSION_EXPIRY = new ConcurrentHashMap<>();

  private static final long LOGIN_CODE_TTL_MS = TimeUnit.MINUTES.toMillis(RedisConstants.LOGIN_CODE_TTL);
  private static final long SESSION_TTL_MS = TimeUnit.SECONDS.toMillis(RedisConstants.LOGIN_USER_TTL);

  private FallbackSessionStore() {
  }

  public static void setLoginCode(String phone, String code) {
    String key = RedisConstants.LOGIN_CODE_KEY + phone;
    LOGIN_CODES.put(key, code);
    CODE_EXPIRY.put(key, System.currentTimeMillis() + LOGIN_CODE_TTL_MS);
  }

  public static String getLoginCode(String phone) {
    String key = RedisConstants.LOGIN_CODE_KEY + phone;
    Long expiry = CODE_EXPIRY.get(key);
    if (expiry != null && System.currentTimeMillis() > expiry) {
      LOGIN_CODES.remove(key);
      CODE_EXPIRY.remove(key);
      return null;
    }
    return LOGIN_CODES.get(key);
  }

  public static void setSession(String token, Map<Object, Object> userMap) {
    String key = RedisConstants.LOGIN_USER_KEY + token;
    SESSIONS.put(key, new ConcurrentHashMap<>(userMap));
    SESSION_EXPIRY.put(key, System.currentTimeMillis() + SESSION_TTL_MS);
  }

  public static Map<Object, Object> getSession(String token) {
    String key = RedisConstants.LOGIN_USER_KEY + token;
    Long expiry = SESSION_EXPIRY.get(key);
    if (expiry != null && System.currentTimeMillis() > expiry) {
      SESSIONS.remove(key);
      SESSION_EXPIRY.remove(key);
      return Map.of();
    }
    Map<Object, Object> map = SESSIONS.get(key);
    return map != null ? map : Map.of();
  }

  public static void refreshSession(String token) {
    String key = RedisConstants.LOGIN_USER_KEY + token;
    if (SESSION_EXPIRY.containsKey(key)) {
      SESSION_EXPIRY.put(key, System.currentTimeMillis() + SESSION_TTL_MS);
    }
  }
}
