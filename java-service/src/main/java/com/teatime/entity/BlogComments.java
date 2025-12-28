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
 * BlogComments entity representing comments on blog posts.
 * </p>
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_blog_comments")
public class BlogComments implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Primary key ID
   */
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /**
   * User id of the commenter
   */
  private Long userId;

  /**
   * Associated blog id
   */
  private Long blogId;

  /**
   * Parent comment id
   */
  private Long parentId;

  /**
   * Replied comment id
   */
  private Long answerId;

  /**
   * Comment content
   */
  private String content;

  /**
   * Number of likes
   */
  private Integer liked;

  /**
   * Comment status
   */
  private Boolean status;

  /**
   * Creation time
   */
  private LocalDateTime createTime;

  /**
   * Update time
   */
  private LocalDateTime updateTime;


}
