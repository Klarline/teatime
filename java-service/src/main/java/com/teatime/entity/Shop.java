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
 * Shop entity representing a shop in the system.
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_shop")
public class Shop implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Primary key ID
   */
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /**
   * Shop name
   */
  private String name;

  /**
   * Shop type ID
   */
  private Long typeId;

  /**
   * Shop images, multiple images separated by commas
   */
  private String images;

  /**
   * Area
   */
  private String area;

  /**
   * Address
   */
  private String address;

  /**
   * Longitude
   */
  private Double x;

  /**
   * Latitude
   */
  private Double y;

  /**
   * Average price per person in cents
   */
  private Long avgPrice;

  /**
   * Sales volume
   */
  private Integer sold;

  /**
   * Number of comments
   */
  private Integer comments;

  /**
   * Shop score
   */
  private Integer score;

  /**
   * Opening hours
   */
  private String openHours;

  /**
   * Creation time
   */
  private LocalDateTime createTime;

  /**
   * Update time
   */
  private LocalDateTime updateTime;

  /**
   * Distance from a certain point, not stored in database
   */
  @TableField(exist = false)
  private Double distance;
}
