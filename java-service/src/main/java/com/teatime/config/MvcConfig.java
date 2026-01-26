package com.teatime.config;

import com.teatime.utils.LoginInterceptor;
import com.teatime.utils.RefreshTokenInterceptor;
import javax.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

  @Resource
  private StringRedisTemplate stringRedisTemplate;

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedOrigins(
            "http://localhost:3000",
            "http://teatime-frontend-claire.s3-website-us-east-1.amazonaws.com"
        )
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(true)
        .maxAge(3600);
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    // Register the refresh token interceptor
    registry.addInterceptor(new RefreshTokenInterceptor(stringRedisTemplate))
        .addPathPatterns("/**").order(0);

    // Register the login interceptor
    registry.addInterceptor(new LoginInterceptor())
        .excludePathPatterns(
            // Auth endpoints
            "/api/user/code",
            "/api/user/login",

            // Public browsing endpoints
            "/api/shop/**",
            "/api/blog/hot",
            "/api/shop-type/**",

            // Public resources
            "/api/coupon/list/**",
            "/api/upload/**",

            // Actuator endpoints
            "/actuator/**"

        ).order(1);
  }
}