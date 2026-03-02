package com.teatime.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Shop Review Entity
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "tb_shop_review")
public class ShopReview implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Primary key
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * Shop ID
   */
  @Column(name = "shop_id")
  private Long shopId;

  /**
   * User ID
   */
  @Column(name = "user_id")
  private Long userId;

  /**
   * Rating (1-5 stars)
   */
  private Integer rating;

  /**
   * Review content
   */
  private String content;

  /**
   * Creation time
   */
  @Column(name = "create_time")
  private LocalDateTime createTime;

  /**
   * Update time
   */
  @Column(name = "update_time")
  private LocalDateTime updateTime;
}
