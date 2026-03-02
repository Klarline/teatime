package com.teatime.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * FlashSaleCoupon entity representing a flash sale coupon in the system.
 * </p>
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "tb_flash_sale_coupon")
public class FlashSaleCoupon implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Coupon ID
   */
  @Id
  @Column(name = "coupon_id")
  private Long couponId;

  /**
   * Stock available for the flash sale coupon
   */
  private Integer stock;

  /**
   * Creation time of the coupon
   */
  private LocalDateTime createTime;

  /**
   * Begin time of the flash sale coupon
   */
  private LocalDateTime beginTime;

  /**
   * End time of the flash sale coupon
   */
  private LocalDateTime endTime;

  /**
   * Last update time of the flash sale coupon
   */
  private LocalDateTime updateTime;

}
