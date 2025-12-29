package com.teatime.dto.ai;

import lombok.Data;

@Data
public class ReviewDocument {
  private Long reviewId;
  private Long shopId;
  private String shopName;
  private String content;
  private String title;
  private String userName;
}