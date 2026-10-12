package com.fishmart.content.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EngagementDto {
  private long likeCount;
  private long favoriteCount;
  private boolean liked;
  private boolean favored;
}
