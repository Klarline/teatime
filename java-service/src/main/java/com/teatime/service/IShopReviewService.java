package com.teatime.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teatime.dto.Result;
import com.teatime.entity.ShopReview;

/**
 * Shop Review Service Interface
 */
public interface IShopReviewService extends IService<ShopReview> {

  /**
   * Create a new review
   *
   * @param review Review object
   * @return Result with review ID
   */
  Result createReview(ShopReview review);

  /**
   * Get reviews for a specific shop with pagination
   *
   * @param shopId  Shop ID
   * @param current Current page number
   * @param size    Page size
   * @return Result with review list
   */
  Result getShopReviews(Long shopId, Integer current, Integer size);

  /**
   * Get shop rating statistics (average rating and review count)
   *
   * @param shopId Shop ID
   * @return Result with rating stats
   */
  Result getShopRatingStats(Long shopId);

  /**
   * Check if current user has reviewed this shop
   *
   * @param shopId Shop ID
   * @return Result with boolean
   */
  Result hasUserReviewed(Long shopId);

  /**
   * Delete a review (only by review owner)
   *
   * @param reviewId Review ID
   * @return Result
   */
  Result deleteReview(Long reviewId);
}