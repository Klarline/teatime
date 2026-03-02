package com.teatime.repository;

import com.teatime.entity.ShopReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * JPA Repository for ShopReview entity
 */
public interface ShopReviewRepository extends JpaRepository<ShopReview, Long> {

  Page<ShopReview> findByShopIdOrderByCreateTimeDesc(Long shopId, Pageable pageable);

  long countByUserIdAndShopId(Long userId, Long shopId);

  boolean existsByUserIdAndShopId(Long userId, Long shopId);

  @Query("SELECT COALESCE(AVG(s.rating), 0), COUNT(s) FROM ShopReview s WHERE s.shopId = :shopId")
  Object[] findAvgRatingAndCountByShopId(@Param("shopId") Long shopId);
}
