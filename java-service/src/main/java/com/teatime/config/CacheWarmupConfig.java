package com.teatime.config;

import com.teatime.entity.Shop;
import com.teatime.service.IShopService;
import com.teatime.utils.CacheClient;
import com.teatime.utils.RedisConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class CacheWarmupConfig {

  @Resource
  private IShopService shopService;

  @Resource
  private CacheClient cacheClient;

  @Resource
  private StringRedisTemplate stringRedisTemplate;

  @Bean
  public CommandLineRunner warmupCache() {
    return args -> {
      try {
        // Get all shops from database
        List<Shop> shops = shopService.list();

        if (shops.isEmpty()) {
          return;
        }

        for (Shop shop : shops) {
          // Warm up shop cache with logical expiration
          cacheClient.setLogicalExpire(
              RedisConstants.CACHE_SHOP_KEY + shop.getId(),
              shop,
              RedisConstants.CACHE_SHOP_TTL,
              TimeUnit.MINUTES
          );

          // Add shop to GEO index by type
          String geoKey = RedisConstants.SHOP_GEO_KEY + shop.getTypeId();
          stringRedisTemplate.opsForGeo().add(
              geoKey,
              new Point(shop.getX(), shop.getY()),
              shop.getId().toString()
          );
        }
        log.info("Cache warmup completed for {} shops", shops.size());
      } catch (Exception e) {
        log.error("Cache pre-warming failed (application will continue): ", e);
      }
    };
  }
}