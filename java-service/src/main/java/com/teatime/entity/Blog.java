package com.teatime.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.*;
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
@Entity
@Table(name = "tb_blog")
public class Blog implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * primary key
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
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
  @Transient
  private String icon;

  /**
   * user name
   */
  @Transient
  private String name;

  /**
   * whether the current user liked this blog
   */
  @Transient
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
