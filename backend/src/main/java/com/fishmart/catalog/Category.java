package com.fishmart.catalog;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "categories")
public class Category {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 64)
  private String name;

  @Column(nullable = false)
  private int sortOrder = 0;

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();
}

