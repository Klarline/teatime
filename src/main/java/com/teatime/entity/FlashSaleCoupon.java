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
 * FlashSaleCoupon entity representing a flash sale coupon in the system.
 * </p>
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_flash_sale_coupon")
public class FlashSaleCoupon implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Coupon ID
   */
  @TableId(value = "coupon_id", type = IdType.INPUT)
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
