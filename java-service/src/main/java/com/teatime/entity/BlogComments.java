package com.teatime.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.*;
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
@Entity
@Table(name = "tb_blog_comments")
public class BlogComments implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Primary key ID
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
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
   * Comment status (0/1 in DB, mapped as Boolean)
   */
  @Column(columnDefinition = "tinyint(1)")
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
