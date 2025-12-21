package com.teatime.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * Coupon entity representing a coupon in the system.
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_coupon")
public class Coupon implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Primary key ID
   */
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /**
   * Shop ID
   */
  private Long shopId;

  /**
   * Coupon title
   */
  private String title;

  /**
   * Coupon subtitle
   */
  private String subTitle;

  /**
   * Coupon rules
   */
  private String rules;

  /**
   * Price
   */
  private Long payValue;

  /**
   * Actual value
   */
  private Long actualValue;

  /**
   * Coupon type
   */
  private Integer type;

  /**
   * Status (0: inactive, 1: active)
   */
  private Integer status;
  /**
   * Stock (not stored in database)
   */
  @TableField(exist = false)
  private Integer stock;

  /**
   * Begin time
   */
  @TableField(exist = false)
  private LocalDateTime beginTime;

  /**
   * End time
   */
  @TableField(exist = false)
  private LocalDateTime endTime;

  /**
   * Creation time
   */
  private LocalDateTime createTime;


  /**
   * Update time
   */
  private LocalDateTime updateTime;

}
