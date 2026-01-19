package com.teatime.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teatime.dto.Result;
import com.teatime.entity.ShopReview;
import com.teatime.service.IShopReviewService;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ShopReviewControllerTest {

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @Mock
  private IShopReviewService reviewService;

  @InjectMocks
  private ShopReviewController reviewController;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(reviewController).build();
    objectMapper = new ObjectMapper();
  }

  /**
   * Test 1: POST /api/review - Create review successfully
   */
  @Test
  void testCreateReview_Success() throws Exception {
    // Arrange
    ShopReview review = new ShopReview();
    review.setShopId(1L);
    review.setRating(5);
    review.setContent("Excellent tea!");

    when(reviewService.createReview(any(ShopReview.class))).thenReturn(Result.ok(1L));

    // Act & Assert
    mockMvc.perform(post("/api/review")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(review)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").value(1L));
  }

  /**
   * Test 2: POST /api/review - Invalid rating fails
   */
  @Test
  void testCreateReview_InvalidRating() throws Exception {
    // Arrange
    ShopReview review = new ShopReview();
    review.setShopId(1L);
    review.setRating(6); // Invalid rating

    when(reviewService.createReview(any(ShopReview.class)))
        .thenReturn(Result.fail("Rating must be between 1 and 5"));

    // Act & Assert
    mockMvc.perform(post("/api/review")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(review)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.errorMsg").value("Rating must be between 1 and 5"));
  }

  /**
   * Test 3: GET /api/review/shop/{shopId} - Get shop reviews
   */
  @Test
  void testGetShopReviews_Success() throws Exception {
    // Arrange
    Long shopId = 1L;
    when(reviewService.getShopReviews(eq(shopId), eq(1), eq(10)))
        .thenReturn(Result.ok(Arrays.asList()));

    // Act & Assert
    mockMvc.perform(get("/api/review/shop/{shopId}", shopId)
            .param("current", "1")
            .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  /**
   * Test 4: GET /api/review/stats/{shopId} - Get rating statistics
   */
  @Test
  void testGetShopRatingStats_Success() throws Exception {
    // Arrange
    Long shopId = 1L;
    Map<String, Object> stats = new HashMap<>();
    stats.put("avgRating", 4.5);
    stats.put("reviewCount", 10L);

    when(reviewService.getShopRatingStats(shopId)).thenReturn(Result.ok(stats));

    // Act & Assert
    mockMvc.perform(get("/api/review/stats/{shopId}", shopId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.avgRating").value(4.5))
        .andExpect(jsonPath("$.data.reviewCount").value(10));
  }

  /**
   * Test 5: GET /api/review/check/{shopId} - Check if user reviewed
   */
  @Test
  void testHasUserReviewed_True() throws Exception {
    // Arrange
    Long shopId = 1L;
    when(reviewService.hasUserReviewed(shopId)).thenReturn(Result.ok(true));

    // Act & Assert
    mockMvc.perform(get("/api/review/check/{shopId}", shopId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").value(true));
  }

  /**
   * Test 6: DELETE /api/review/{id} - Delete review successfully
   */
  @Test
  void testDeleteReview_Success() throws Exception {
    // Arrange
    Long reviewId = 1L;
    when(reviewService.deleteReview(reviewId)).thenReturn(Result.ok());

    // Act & Assert
    mockMvc.perform(delete("/api/review/{id}", reviewId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }
}