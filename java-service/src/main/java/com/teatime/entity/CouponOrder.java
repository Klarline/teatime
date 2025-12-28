package com.teatime.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * CouponOrder entity representing a coupon order in the system.
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_coupon_order")
public class CouponOrder implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * primary key ID
   */
  @TableId(value = "id", type = IdType.INPUT)
  private Long id;

  /**
   * User id who placed the order
   */
  private Long userId;

  /**
   * Coupon id associated with the order
   */
  private Long couponId;

  /**
   * Payment type
   */
  private Integer payType;

  /**
   * Order status: 0: pending payment, 1: paid, 2: used, 3: refunded
   */
  private Integer status;

  /**
   * Creation time of the order
   */
  private LocalDateTime createTime;

  /**
   * Payment time
   */
  private LocalDateTime payTime;

  /**
   * Usage time
   */
  private LocalDateTime useTime;

  /**
   * Refund time
   */
  private LocalDateTime refundTime;

  /**
   * Update time of the order
   */
  private LocalDateTime updateTime;

}
