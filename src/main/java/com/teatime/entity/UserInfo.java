package com.teatime.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
@TableName("tb_user_info")
public class UserInfo implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * User ID
   */
  @TableId(value = "user_id", type = IdType.AUTO)
  private Long userId;

  /**
   * City
   */
  private String city;

  /**
   * Introduction
   */
  private String introduce;

  /**
   * Number of fans
   */
  private Integer fans;

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
