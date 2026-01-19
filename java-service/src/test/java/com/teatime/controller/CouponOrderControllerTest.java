package com.teatime.controller;

import com.teatime.dto.Result;
import com.teatime.service.ICouponOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CouponOrderControllerTest {

  private MockMvc mockMvc;

  @Mock
  private ICouponOrderService couponOrderService;

  @InjectMocks
  private CouponOrderController couponOrderController;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(couponOrderController).build();
  }

  /**
   * Test 1: POST /api/coupon-order/flash-sale/{id} - Successful purchase
   */
  @Test
  void testFlashSaleCoupon_Success() throws Exception {
    // Arrange
    Long couponId = 1L;
    when(couponOrderService.flashSaleCoupon(couponId)).thenReturn(Result.ok(123456L));

    // Act & Assert
    mockMvc.perform(post("/api/coupon-order/flash-sale/{id}", couponId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").value(123456L));
  }

  /**
   * Test 2: POST /api/coupon-order/flash-sale/{id} - Insufficient stock
   */
  @Test
  void testFlashSaleCoupon_InsufficientStock() throws Exception {
    // Arrange
    Long couponId = 1L;
    when(couponOrderService.flashSaleCoupon(couponId))
        .thenReturn(Result.fail("Insufficient stock"));

    // Act & Assert
    mockMvc.perform(post("/api/coupon-order/flash-sale/{id}", couponId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.errorMsg").value("Insufficient stock"));
  }
}