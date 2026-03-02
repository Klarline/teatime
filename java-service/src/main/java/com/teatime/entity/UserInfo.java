package com.teatime.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * UserInfo entity representing additional information about a user.
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "tb_user_info")
public class UserInfo implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * User ID
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id")
  private Long userId;

  /**
   * City
   */
  private String city;

  /**
   * Introduction
   */
  private String bio;

  /**
   * Number of followers
   */
  private Integer follower;

  /**
   * Number of followees
   */
  private Integer followee;

  /**
   * Gender
   */
  private Boolean gender;

  /**
   * Birthday
   */
  private LocalDate birthday;

  /**
   * Credits
   */
  private Integer credits;

  /**
   * User level
   */
  private Boolean level;

  /**
   * Creation time
   */
  private LocalDateTime createTime;

  /**
   * Update time
   */
  private LocalDateTime updateTime;

}
