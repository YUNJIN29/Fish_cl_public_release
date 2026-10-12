package com.fishmart.waterquality;

import com.fishmart.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter @Setter @Entity
@Table(name = "water_quality_targets", uniqueConstraints = @UniqueConstraint(name = "uk_water_quality_target_user_pond", columnNames = {"user_id", "pond_name"}))
public class WaterQualityTarget {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private User user;
  @Column(nullable = false, length = 64) private String pondName;
  @Column(precision = 6, scale = 2) private BigDecimal temperatureMin;
  @Column(precision = 6, scale = 2) private BigDecimal temperatureMax;
  @Column(precision = 5, scale = 2) private BigDecimal phMin;
  @Column(precision = 5, scale = 2) private BigDecimal phMax;
  @Column(precision = 7, scale = 3) private BigDecimal dissolvedOxygenMin;
  @Column(precision = 8, scale = 3) private BigDecimal ammoniaMax;
  @Column(precision = 8, scale = 3) private BigDecimal nitriteMax;
  @Column(nullable = false, updatable = false) private Instant createdAt = Instant.now();
  @Column(nullable = false) private Instant updatedAt = Instant.now();
}
