package com.teatime.repository;

import com.teatime.entity.CouponOrder;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA Repository for CouponOrder entity
 */
public interface CouponOrderRepository extends JpaRepository<CouponOrder, Long> {

  long countByUserIdAndCouponId(Long userId, Long couponId);
}
