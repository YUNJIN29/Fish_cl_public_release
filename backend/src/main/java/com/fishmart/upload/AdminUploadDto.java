package com.fishmart.upload;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class AdminUploadDto {
  private String id;
  private String originalName;
  private String contentType;
  private long sizeBytes;
  private Instant createdAt;
  private Long ownerUserId;
  private String ownerUsername;
  private String resourceUrl;
}
