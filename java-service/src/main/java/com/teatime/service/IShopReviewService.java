package com.teatime.service;

import com.teatime.dto.Result;
import com.teatime.entity.ShopReview;

/**
 * Shop Review Service Interface
 */
public interface IShopReviewService {

  Result createReview(ShopReview review);

  Result getShopReviews(Long shopId, Integer current, Integer size);

  Result getShopRatingStats(Long shopId);

  Result hasUserReviewed(Long shopId);

  Result deleteReview(Long reviewId);
}
