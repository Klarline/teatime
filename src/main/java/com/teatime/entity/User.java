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
 * User table
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_user")
public class User implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Primary key ID
   */
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /**
   * Phone number
   */
  private String phone;

  /**
   * Password, encrypted
   */
  private String password;

  /**
   * Nickname
   */
  private String nickName;

  /**
   * Icon
   */
  private String icon = "";

  /**
   * Creation time
   */
  private LocalDateTime createTime;

  /**
   * Update time
   */
  private LocalDateTime updateTime;

}
