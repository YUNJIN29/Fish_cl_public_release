package com.fishmart.catalog;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "products")
public class Product {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "category_id", nullable = false)
  private Category category;

  @Column(nullable = false, length = 128)
  private String name;

  @Lob
  private String description;

  @Column(nullable = false)
  private int priceCents;

  @Column(nullable = false)
  private int stock = 0;

  @Column(length = 512)
  private String coverImageUrl;

  @Column(length = 256)
  private String symptomTags; // 用于匹配诊断症状，如 "白点,烂鳍,竖鳞"

  @Lob
  private String usageAdvice; // 使用建议/养殖贴士

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private ProductStatus status = ProductStatus.ON;

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();
}

