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
 * Blog entity representing a blog post in the system.
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_blog")
public class Blog implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * primary key
   */
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /**
   * shop id
   */
  private Long shopId;

  /**
   * user id
   */
  private Long userId;

  /**
   * user icon
   */
  @TableField(exist = false)
  private String icon;

  /**
   * user name
   */
  @TableField(exist = false)
  private String name;

  /**
   * whether the current user liked this blog
   */
  @TableField(exist = false)
  private Boolean isLike;

  /**
   * title of the blog
   */
  private String title;

  /**
   * images of the blog, separated by commas
   */
  private String images;

  /**
   * content of the blog
   */
  private String content;

  /**
   * number of likes
   */
  private Integer liked;

  /**
   * number of comments
   */
  private Integer comments;

  /**
   * creation time
   */
  private LocalDateTime createTime;

  /**
   * update time
   */
  private LocalDateTime updateTime;


}
