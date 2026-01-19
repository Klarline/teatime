package com.teatime.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teatime.dto.Result;
import com.teatime.entity.Shop;
import com.teatime.service.IShopService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ShopControllerTest {

  private MockMvc mockMvc;

  private ObjectMapper objectMapper;

  @Mock
  private IShopService shopService;

  @InjectMocks
  private ShopController shopController;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(shopController).build();
    objectMapper = new ObjectMapper();
  }

  /**
   * Test 1: GET /api/shop/{id} - Get shop by ID successfully
   */
  @Test
  void testQueryShopById_Success() throws Exception {
    // Arrange
    Long shopId = 1L;
    Shop shop = createTestShop(shopId, "Test Tea Shop");
    when(shopService.queryById(shopId)).thenReturn(Result.ok(shop));

    // Act & Assert
    mockMvc.perform(get("/api/shop/{id}", shopId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.id").value(shopId))
        .andExpect(jsonPath("$.data.name").value("Test Tea Shop"));
  }

  /**
   * Test 2: GET /api/shop/{id} - Shop not found
   */
  @Test
  void testQueryShopById_NotFound() throws Exception {
    // Arrange
    Long shopId = 999L;
    when(shopService.queryById(shopId)).thenReturn(Result.fail("Shop does not exist!"));

    // Act & Assert
    mockMvc.perform(get("/api/shop/{id}", shopId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.errorMsg").value("Shop does not exist!"));
  }

  /**
   * Test 3: PUT /api/shop - Update shop successfully
   */
  @Test
  void testUpdateShop_Success() throws Exception {
    // Arrange
    Shop shop = createTestShop(1L, "Updated Tea Shop");
    when(shopService.update(any(Shop.class))).thenReturn(Result.ok());

    // Act & Assert
    mockMvc.perform(put("/api/shop")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(shop)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  /**
   * Test 4: PUT /api/shop - Update with null ID fails
   */
  @Test
  void testUpdateShop_NullId() throws Exception {
    // Arrange
    Shop shop = createTestShop(null, "Shop Without ID");
    when(shopService.update(any(Shop.class)))
        .thenReturn(Result.fail("Shop id cannot be null!"));

    // Act & Assert
    mockMvc.perform(put("/api/shop")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(shop)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.errorMsg").value("Shop id cannot be null!"));
  }

  /**
   * Test 5: GET /api/shop/of/type - Query shops by type without coordinates
   */
  @Test
  void testQueryShopByType_WithoutCoordinates() throws Exception {
    // Arrange
    List<Shop> shops = Arrays.asList(
        createTestShop(1L, "Shop 1"),
        createTestShop(2L, "Shop 2")
    );
    when(shopService.queryShopByType(eq(1), eq(1), isNull(), isNull()))
        .thenReturn(Result.ok(shops));

    // Act & Assert
    mockMvc.perform(get("/api/shop/of/type")
            .param("typeId", "1")
            .param("current", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.length()").value(2))
        .andExpect(jsonPath("$.data[0].name").value("Shop 1"));
  }

  // Helper method
  private Shop createTestShop(Long id, String name) {
    Shop shop = new Shop();
    shop.setId(id);
    shop.setName(name);
    shop.setTypeId(1L);
    shop.setArea("Downtown");
    shop.setAddress("123 Main St");
    return shop;
  }

  /**
   * Test 6: POST /api/shop - Save shop successfully
   */
  @Test
  void testSaveShop_Success() throws Exception {
    // Arrange
    Shop shop = createTestShop(null, "New Tea Shop");

    when(shopService.save(any(Shop.class))).thenAnswer(invocation -> {
      Shop savedShop = invocation.getArgument(0);
      savedShop.setId(1L);
      return true;
    });

    // Act & Assert
    mockMvc.perform(post("/api/shop")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(shop)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").value(1L));
  }
}