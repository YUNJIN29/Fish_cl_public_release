package com.fishmart.waterquality;

import com.fishmart.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter @Setter @Entity
@Table(name = "water_quality_records")
public class WaterQualityRecord {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private User user;
  @Column(nullable = false, length = 64) private String pondName;
  @Column(precision = 6, scale = 2) private BigDecimal temperature;
  @Column(precision = 5, scale = 2) private BigDecimal ph;
  @Column(precision = 7, scale = 3) private BigDecimal dissolvedOxygen;
  @Column(precision = 8, scale = 3) private BigDecimal ammonia;
  @Column(precision = 8, scale = 3) private BigDecimal nitrite;
  @Column(length = 500) private String notes;
  @Column(nullable = false) private Instant observedAt;
  @Column(nullable = false, updatable = false) private Instant createdAt = Instant.now();
}
