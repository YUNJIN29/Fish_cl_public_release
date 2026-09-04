package com.fishmart.rag;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "rag_documents")
public class RagDocument {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 180)
  private String title;

  @Column(length = 80)
  private String sourceName;

  @Column(length = 512)
  private String sourceUrl;

  @Column(length = 64)
  private String sourceType;

  @Column(length = 32)
  private String language;

  @Column(length = 64)
  private String category;

  @Column(length = 512)
  private String tags;

  @Lob
  private String summary;

  @Lob
  private String content;

  @Column(nullable = false)
  private boolean enabled = true;

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();
}
