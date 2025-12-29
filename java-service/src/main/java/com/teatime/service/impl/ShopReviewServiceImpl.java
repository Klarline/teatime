package com.teatime.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teatime.dto.Result;
import com.teatime.dto.ai.ReviewDocument;
import com.teatime.entity.Shop;
import com.teatime.entity.ShopReview;
import com.teatime.entity.User;
import com.teatime.mapper.ShopMapper;
import com.teatime.mapper.ShopReviewMapper;
import com.teatime.mapper.UserMapper;
import com.teatime.service.IAIService;
import com.teatime.service.IShopReviewService;
import com.teatime.service.IShopService;
import com.teatime.utils.UserHolder;
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
@Service
public class ShopReviewServiceImpl extends ServiceImpl<ShopReviewMapper, ShopReview>
    implements IShopReviewService {

  @Resource
  private UserMapper userMapper;

  @Resource
  private ShopMapper shopMapper;

  @Resource
  private IAIService aiService;

  @Resource
  private IShopService shopService;


  @Override
  public Result createReview(ShopReview review) {
    // Get current user
    Long userId = UserHolder.getUser().getId();

    // Check if user already reviewed this shop
    LambdaQueryWrapper<ShopReview> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(ShopReview::getUserId, userId)
        .eq(ShopReview::getShopId, review.getShopId());
    Long count = baseMapper.selectCount(wrapper);

    if (count > 0) {
      return Result.fail("You have already reviewed this shop");
    }

    if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
      return Result.fail("Rating must be between 1 and 5");
    }

    // Set user ID and timestamps
    review.setUserId(userId);
    review.setCreateTime(LocalDateTime.now());
    review.setUpdateTime(LocalDateTime.now());

    boolean success = save(review);

    if (!success) {
      return Result.fail("Failed to create review");
    }

    // Update shop rating and comment count
    updateShopRating(review.getShopId());

    // Async ingestion to AI service
    try {
      ReviewDocument reviewDoc = new ReviewDocument();
      reviewDoc.setReviewId(review.getId());
      reviewDoc.setShopId(review.getShopId());
      reviewDoc.setContent(review.getContent());
      reviewDoc.setTitle("Rating: " + review.getRating() + "/5"); // Include rating in title

      // Get shop name
      Shop shop = shopService.getById(review.getShopId());
      if (shop != null) {
        reviewDoc.setShopName(shop.getName());
      }

      // Get username
      User user = userMapper.selectById(review.getUserId());
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
    // Build query with pagination
    LambdaQueryWrapper<ShopReview> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(ShopReview::getShopId, shopId)
        .orderByDesc(ShopReview::getCreateTime);

    // Calculate offset
    int offset = (current - 1) * size;
    wrapper.last("LIMIT " + offset + ", " + size);

    List<ShopReview> reviews = baseMapper.selectList(wrapper);

    // Enrich with user information
    List<Map<String, Object>> enrichedReviews = reviews.stream().map(review -> {
      Map<String, Object> reviewMap = BeanUtil.beanToMap(review);

      User user = userMapper.selectById(review.getUserId());
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
    // Get average rating using QueryWrapper
    QueryWrapper<ShopReview> avgWrapper = new QueryWrapper<>();
    avgWrapper.select("IFNULL(AVG(rating), 0) as avgRating, COUNT(*) as reviewCount")
        .eq("shop_id", shopId);

    Map<String, Object> stats = baseMapper.selectMaps(avgWrapper).get(0);

    // Round average to 1 decimal place
    Double avgRating = (Double) stats.get("avgRating");
    Long reviewCount = (Long) stats.get("reviewCount");

    Map<String, Object> result = new HashMap<>();
    result.put("avgRating", Math.round(avgRating * 10.0) / 10.0);
    result.put("reviewCount", reviewCount);

    return Result.ok(result);
  }

  @Override
  public Result hasUserReviewed(Long shopId) {
    Long userId = UserHolder.getUser().getId();

    LambdaQueryWrapper<ShopReview> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(ShopReview::getUserId, userId)
        .eq(ShopReview::getShopId, shopId);

    Long count = baseMapper.selectCount(wrapper);

    return Result.ok(count > 0);
  }

  @Override
  public Result deleteReview(Long reviewId) {
    Long userId = UserHolder.getUser().getId();

    // Check if review exists and belongs to current user
    ShopReview review = baseMapper.selectById(reviewId);

    if (review == null) {
      return Result.fail("Review not found");
    }

    if (!review.getUserId().equals(userId)) {
      return Result.fail("You can only delete your own reviews");
    }

    // Store shopId before deletion
    Long shopId = review.getShopId();

    // Delete review
    boolean success = removeById(reviewId);

    if (!success) {
      return Result.fail("Failed to delete review");
    }

    // Update shop rating and comment count
    updateShopRating(shopId);

    return Result.ok();
  }

  /**
   * Update shop's average rating and review count
   *
   * @param shopId Shop ID
   */
  private void updateShopRating(Long shopId) {
    // Get current stats
    QueryWrapper<ShopReview> wrapper = new QueryWrapper<>();
    wrapper.select("IFNULL(AVG(rating), 0) as avgRating, COUNT(*) as reviewCount")
        .eq("shop_id", shopId);

    Map<String, Object> stats = baseMapper.selectMaps(wrapper).get(0);
    Double avgRating = (Double) stats.get("avgRating");
    Long reviewCount = (Long) stats.get("reviewCount");

    // Update shop
    Shop shop = shopMapper.selectById(shopId);
    if (shop != null) {
      // Round to 1 decimal place
      shop.setScore((int) Math.round(avgRating * 10.0));
      shop.setComments(reviewCount.intValue());
      shopMapper.updateById(shop);
    }
  }
}