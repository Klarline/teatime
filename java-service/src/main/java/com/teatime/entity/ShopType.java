package com.teatime.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * ShopType entity representing types of shops.
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "tb_shop_type")
public class ShopType implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Primary key ID
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * Type name
   */
  private String name;

  /**
   * Icon
   */
  private String icon;

  /**
   * Sort order
   */
  private Integer sort;

  /**
   * Creation time
   */
  @JsonIgnore
  private LocalDateTime createTime;

  /**
   * Update time
   */
  @JsonIgnore
  private LocalDateTime updateTime;

}
