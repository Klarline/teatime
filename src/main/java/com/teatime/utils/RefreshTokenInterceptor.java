package com.teatime.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.teatime.dto.UserDTO;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

public class RefreshTokenInterceptor implements HandlerInterceptor {

  private StringRedisTemplate stringRedisTemplate;

  public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    // get token from request header
    String token = request.getHeader("authorization");
    if (StrUtil.isBlank(token)) {
      return true;
    }

    // get user information from Redis
    String key = RedisConstants.LOGIN_USER_KEY + token;
    Map<Object, Object>
        userMap = stringRedisTemplate.opsForHash().entries(key);
    if (userMap.isEmpty()) {
      return true;
    }
    UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);

    // save user information to ThreadLocal
    UserHolder.saveUser((UserDTO) userDTO);

    // refresh token expiration time
    stringRedisTemplate.expire(key, RedisConstants.LOGIN_USER_TTL, TimeUnit.SECONDS);

    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                              Object handler, Exception ex) throws Exception {
    // remove user information from ThreadLocal
    UserHolder.removeUser();
  }
}
