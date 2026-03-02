package com.teatime.repository;

import com.teatime.entity.FlashSaleCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * JPA Repository for FlashSaleCoupon entity
 */
public interface FlashSaleCouponRepository extends JpaRepository<FlashSaleCoupon, Long> {

  @Modifying
  @Query("UPDATE FlashSaleCoupon f SET f.stock = f.stock - 1 WHERE f.couponId = :couponId AND f.stock > 0")
  int decrementStock(@Param("couponId") Long couponId);
}
