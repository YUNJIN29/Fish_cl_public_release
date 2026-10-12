package com.fishmart.upload;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "upload_resources")
public class UploadResource {
  @Id
  @Column(length = 36)
  private String id;
  @Column(name = "owner_user_id", nullable = false)
  private Long ownerUserId;
  @Column(name = "original_name", nullable = false, length = 255)
  private String originalName;
  @Column(name = "stored_name", nullable = false, unique = true, length = 100)
  private String storedName;
  @Column(name = "content_type", nullable = false, length = 80)
  private String contentType;
  @Column(name = "size_bytes", nullable = false)
  private long sizeBytes;
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();
}
