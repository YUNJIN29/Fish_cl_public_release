package com.fishmart.portfolio;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "portfolio_profile")
public class PortfolioProfile {
  @Id
  private Long id = 1L;

  @Lob
  @Column(nullable = false, columnDefinition = "LONGTEXT")
  private String contentJson;

  @Column(nullable = false)
  private Instant updatedAt = Instant.now();

  @PreUpdate
  public void touch() { updatedAt = Instant.now(); }
}
