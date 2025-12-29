package com.teatime.controller;

import com.teatime.dto.Result;
import com.teatime.entity.ShopReview;
import com.teatime.service.IShopReviewService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * Shop Review Controller
 */
@RestController
@RequestMapping("/api/review")
public class ShopReviewController {

  @Resource
  private IShopReviewService reviewService;

  /**
   * Create a new review
   * POST /api/review
   * <p>
   * Body: { "shopId": 1, "rating": 5, "content": "Great place!" }
   */
  @PostMapping
  public Result createReview(@RequestBody ShopReview review) {
    // Service layer handles validation
    return reviewService.createReview(review);
  }

  /**
   * Get reviews for a specific shop
   * GET /api/review/shop/{shopId}?current=1&size=10
   */
  @GetMapping("/shop/{shopId}")
  public Result getShopReviews(
      @PathVariable("shopId") Long shopId,
      @RequestParam(value = "current", defaultValue = "1") Integer current,
      @RequestParam(value = "size", defaultValue = "10") Integer size) {

    return reviewService.getShopReviews(shopId, current, size);
  }

  /**
   * Get shop rating statistics
   * GET /api/review/stats/{shopId}
   * <p>
   * Returns: { avgRating: 4.5, reviewCount: 23 }
   */
  @GetMapping("/stats/{shopId}")
  public Result getShopRatingStats(@PathVariable("shopId") Long shopId) {
    return reviewService.getShopRatingStats(shopId);
  }

  /**
   * Check if current user has reviewed this shop
   * GET /api/review/check/{shopId}
   */
  @GetMapping("/check/{shopId}")
  public Result hasUserReviewed(@PathVariable("shopId") Long shopId) {
    return reviewService.hasUserReviewed(shopId);
  }

  /**
   * Delete a review
   * DELETE /api/review/{id}
   */
  @DeleteMapping("/{id}")
  public Result deleteReview(@PathVariable("id") Long id) {
    return reviewService.deleteReview(id);
  }
}