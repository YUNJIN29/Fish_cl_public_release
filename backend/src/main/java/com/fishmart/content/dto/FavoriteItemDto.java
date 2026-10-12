package com.fishmart.content.dto;

import com.fishmart.content.TargetType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class FavoriteItemDto {
  private Long id;
  private TargetType targetType;
  private Long targetId;
  private String title;
  private String summary;
  private String coverImageUrl;
  private Instant createdAt;
  private Instant targetCreatedAt;
  private boolean available;
  private String url;
}
