package com.fishmart.portfolio;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "portfolio_certificates")
public class PortfolioCertificate {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 32)
  private String type;

  @Column(nullable = false, length = 64)
  private String level;

  @Column(nullable = false, length = 255)
  private String title;

  @Column(nullable = false, length = 512)
  private String url;

  @Column(nullable = false)
  private Instant createdAt = Instant.now();
}
