package com.teatime.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.*;
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
@Entity
@Table(name = "tb_coupon")
public class Coupon implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Primary key ID
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
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
  @Transient
  private Integer stock;

  /**
   * Begin time
   */
  @Transient
  private LocalDateTime beginTime;

  /**
   * End time
   */
  @Transient
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
