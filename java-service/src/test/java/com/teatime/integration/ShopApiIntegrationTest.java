package com.teatime.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.teatime.entity.Shop;
import com.teatime.repository.ShopRepository;
import com.teatime.utils.CacheClient;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Integration test for Shop API - tests full request/response cycle with real
 * Spring context and in-memory H2 database. Requires Redis to be running locally
 * (or set spring.redis.host/port in test profile).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Disabled("Requires Redis - enable when running: docker run -d -p 6379:6379 redis:7-alpine")
class ShopApiIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ShopRepository shopRepository;

  @MockBean
  private CacheClient cacheClient;

  @BeforeEach
  void setUp() {
    shopRepository.deleteAll();
    // Simulate cache miss - delegate to DB fallback
    when(cacheClient.queryWithLogicalExpire(anyString(), anyLong(), eq(Shop.class), any(), anyLong(), any(TimeUnit.class)))
        .thenAnswer(invocation -> {
          java.util.function.Function<Long, Shop> fallback = invocation.getArgument(3);
          Long id = invocation.getArgument(1);
          return fallback.apply(id);
        });
  }

  @Test
  void testQueryShopById_WhenExists_ReturnsShop() throws Exception {
    Shop shop = new Shop();
    shop.setName("Integration Test Shop");
    shop.setTypeId(1L);
    shop.setArea("Test Area");
    shop.setAddress("123 Test St");
    shop = shopRepository.save(shop);

    mockMvc.perform(get("/api/shop/{id}", shop.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.name").value("Integration Test Shop"));
  }

  @Test
  void testQueryShopById_WhenNotExists_ReturnsFail() throws Exception {
    mockMvc.perform(get("/api/shop/{id}", 99999))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.errorMsg").value("Shop does not exist!"));
  }

  @Test
  void testQueryShopByType_ReturnsShops() throws Exception {
    Shop shop = new Shop();
    shop.setName("Type 1 Shop");
    shop.setTypeId(1L);
    shop.setArea("Area");
    shop.setAddress("Address");
    shopRepository.save(shop);

    mockMvc.perform(get("/api/shop/of/type")
            .param("typeId", "1")
            .param("current", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isArray());
  }
}
