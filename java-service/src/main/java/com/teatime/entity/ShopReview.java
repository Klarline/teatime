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
 * Shop Review Entity
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_shop_review")
public class ShopReview implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Primary key
   */
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /**
   * Shop ID
   */
  @TableField("shop_id")
  private Long shopId;

  /**
   * User ID
   */
  @TableField("user_id")
  private Long userId;

  /**
   * Rating (1-5 stars)
   */
  @TableField("rating")
  private Integer rating;

  /**
   * Review content
   */
  @TableField("content")
  private String content;

  /**
   * Creation time
   */
  @TableField("create_time")
  private LocalDateTime createTime;

  /**
   * Update time
   */
  @TableField("update_time")
  private LocalDateTime updateTime;
}