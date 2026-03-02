package com.teatime.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teatime.dto.Result;
import com.teatime.dto.ai.ReviewDocument;
import com.teatime.entity.Shop;
import com.teatime.entity.ShopReview;
import com.teatime.entity.User;
import com.teatime.repository.ShopReviewRepository;
import com.teatime.repository.UserRepository;
import com.teatime.service.IAIService;
import com.teatime.service.IShopReviewService;
import com.teatime.service.IShopService;
import com.teatime.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Shop Review Service Implementation
 */
@Slf4j
@Service
public class ShopReviewServiceImpl implements IShopReviewService {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Resource
  private ShopReviewRepository shopReviewRepository;

  @Resource
  private UserRepository userRepository;

  @Resource
  private IShopService shopService;

  @Resource
  private IAIService aiService;

  @Override
  public Result createReview(ShopReview review) {
    Long userId = UserHolder.getUser().getId();

    if (shopReviewRepository.existsByUserIdAndShopId(userId, review.getShopId())) {
      return Result.fail("You have already reviewed this shop");
    }

    if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
      return Result.fail("Rating must be between 1 and 5");
    }

    review.setUserId(userId);
    review.setCreateTime(LocalDateTime.now());
    review.setUpdateTime(LocalDateTime.now());

    ShopReview saved = shopReviewRepository.save(review);
    if (saved == null) {
      return Result.fail("Failed to create review");
    }

    updateShopRating(review.getShopId());

    try {
      ReviewDocument reviewDoc = new ReviewDocument();
      reviewDoc.setReviewId(review.getId());
      reviewDoc.setShopId(review.getShopId());
      reviewDoc.setContent(review.getContent());
      reviewDoc.setTitle("Rating: " + review.getRating() + "/5");

      Shop shop = shopService.getById(review.getShopId());
      if (shop != null) {
        reviewDoc.setShopName(shop.getName());
      }

      User user = userRepository.findById(review.getUserId()).orElse(null);
      if (user != null) {
        reviewDoc.setUserName(user.getNickName());
      }

      aiService.ingestReview(reviewDoc);
    } catch (Exception e) {
      log.error("Failed to ingest review to AI service", e);
    }

    return Result.ok(review.getId());
  }

  @Override
  public Result getShopReviews(Long shopId, Integer current, Integer size) {
    List<ShopReview> reviews = shopReviewRepository.findByShopIdOrderByCreateTimeDesc(
        shopId,
        PageRequest.of(current - 1, size)
    ).getContent();

    List<Map<String, Object>> enrichedReviews = reviews.stream().map(review -> {
      Map<String, Object> reviewMap = OBJECT_MAPPER.convertValue(review,
          new TypeReference<Map<String, Object>>() {});

      User user = userRepository.findById(review.getUserId()).orElse(null);
      if (user != null) {
        reviewMap.put("nickName", user.getNickName());
        reviewMap.put("icon", user.getIcon());
      }

      return reviewMap;
    }).collect(Collectors.toList());

    return Result.ok(enrichedReviews);
  }

  @Override
  public Result getShopRatingStats(Long shopId) {
    Object[] stats = shopReviewRepository.findAvgRatingAndCountByShopId(shopId);

    Object avgRatingObj = stats[0];
    double avgRating = avgRatingObj instanceof Number
        ? ((Number) avgRatingObj).doubleValue()
        : 0.0;
    long reviewCount = (Long) stats[1];

    Map<String, Object> result = new HashMap<>();
    result.put("avgRating", Math.round(avgRating * 10.0) / 10.0);
    result.put("reviewCount", reviewCount);

    return Result.ok(result);
  }

  @Override
  public Result hasUserReviewed(Long shopId) {
    Long userId = UserHolder.getUser().getId();
    boolean exists = shopReviewRepository.existsByUserIdAndShopId(userId, shopId);
    return Result.ok(exists);
  }

  @Override
  public Result deleteReview(Long reviewId) {
    Long userId = UserHolder.getUser().getId();

    ShopReview review = shopReviewRepository.findById(reviewId).orElse(null);
    if (review == null) {
      return Result.fail("Review not found");
    }

    if (!review.getUserId().equals(userId)) {
      return Result.fail("You can only delete your own reviews");
    }

    Long shopId = review.getShopId();
    shopReviewRepository.deleteById(reviewId);
    updateShopRating(shopId);

    return Result.ok();
  }

  private void updateShopRating(Long shopId) {
    Object[] stats = shopReviewRepository.findAvgRatingAndCountByShopId(shopId);

    Object avgRatingObj = stats[0];
    double avgRating = avgRatingObj instanceof Number
        ? ((Number) avgRatingObj).doubleValue()
        : 0.0;
    long reviewCount = (Long) stats[1];

    Shop shop = shopService.getById(shopId);
    if (shop != null) {
      shop.setScore((int) Math.round(avgRating * 10.0));
      shop.setComments((int) reviewCount);
      shopService.update(shop);
    }
  }
}
