package com.teatime.repository;

import com.teatime.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * JPA Repository for Coupon entity
 */
public interface CouponRepository extends JpaRepository<Coupon, Long> {

  List<Coupon> findByShopIdAndStatusOrderByCreateTimeDesc(Long shopId, Integer status);
}
