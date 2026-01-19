package com.teatime.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teatime.dto.Result;
import com.teatime.entity.Coupon;
import com.teatime.service.ICouponService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CouponControllerTest {

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @Mock
  private ICouponService couponService;

  @InjectMocks
  private CouponController couponController;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(couponController).build();
    objectMapper = new ObjectMapper();
  }

  /**
   * Test 1: POST /api/coupon - Add regular coupon
   */
  @Test
  void testAddCoupon_Success() throws Exception {
    // Arrange
    Coupon coupon = new Coupon();
    coupon.setShopId(1L);
    coupon.setTitle("50% Off");
    coupon.setStock(100);

    when(couponService.save(any(Coupon.class))).thenAnswer(invocation -> {
      Coupon c = invocation.getArgument(0);
      c.setId(1L);
      return true;
    });

    // Act & Assert
    mockMvc.perform(post("/api/coupon")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(coupon)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").value(1L));
  }

  /**
   * Test 2: POST /api/coupon/flash-sale - Add flash sale coupon
   */
  @Test
  void testAddFlashSaleCoupon_Success() throws Exception {
    // Arrange
    Coupon coupon = new Coupon();
    coupon.setId(2L);
    coupon.setShopId(1L);
    coupon.setTitle("Flash Sale!");
    coupon.setStock(50);

    // Act & Assert
    mockMvc.perform(post("/api/coupon/flash-sale")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(coupon)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  /**
   * Test 3: GET /api/coupon/list/{shopId} - Get coupons for shop
   */
  @Test
  void testQueryCouponOfShop_Success() throws Exception {
    // Arrange
    Long shopId = 1L;
    when(couponService.queryCouponOfShop(shopId)).thenReturn(Result.ok(Arrays.asList()));

    // Act & Assert
    mockMvc.perform(get("/api/coupon/list/{shopId}", shopId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }
}