package com.teatime.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teatime.dto.UserDTO;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

public class RefreshTokenInterceptor implements HandlerInterceptor {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private StringRedisTemplate stringRedisTemplate;

  public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    // get token from request header
    String token = request.getHeader("authorization");
    if (StringUtils.isBlank(token)) {
      return true;
    }

    // get user information from Redis (falls back to in-memory store when Redis unavailable)
    Map<Object, Object> userMap = RedisFallback.execute(
        () -> stringRedisTemplate.opsForHash().entries(RedisConstants.LOGIN_USER_KEY + token),
        () -> FallbackSessionStore.getSession(token)
    );
    if (userMap == null || userMap.isEmpty()) {
      return true;
    }
    Map<String, Object> stringKeyMap = new HashMap<>();
    userMap.forEach((k, v) -> stringKeyMap.put(k.toString(), v));
    UserDTO userDTO = OBJECT_MAPPER.convertValue(stringKeyMap, UserDTO.class);

    // save user information to ThreadLocal
    UserHolder.saveUser(userDTO);

    // refresh token expiration time
    RedisFallback.executeWithStatus(
        () -> stringRedisTemplate.expire(
            RedisConstants.LOGIN_USER_KEY + token,
            RedisConstants.LOGIN_USER_TTL,
            TimeUnit.SECONDS),
        () -> FallbackSessionStore.refreshSession(token)
    );

    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                              Object handler, Exception ex) throws Exception {
    // remove user information from ThreadLocal
    UserHolder.removeUser();
  }
}
