package com.fishmart.content;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "articles")
public class Article {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(length = 512)
  private String coverImageUrl;

  @Lob
  @Column(nullable = false, columnDefinition = "LONGTEXT")
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private ContentStatus status = ContentStatus.APPROVED;

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();
}

