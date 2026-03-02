package com.teatime.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * Follow entity representing a user's follow relationship with another user.
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "tb_follow")
public class Follow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Primary key
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * User id
   */
  @Column(name = "user_id")
  private Long userId;

  /**
   * Followed user id
   */
  @Column(name = "followed_user_id")
  private Long followUserId;

  /**
   * Creation time of the follow relationship
   */
  private LocalDateTime createTime;

}
