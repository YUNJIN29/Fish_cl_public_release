package com.fishmart.visit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "daily_visits", uniqueConstraints = @UniqueConstraint(name = "uk_visit_date_ip", columnNames = {"visit_date", "ip_address"}))
public class DailyVisit {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "visit_date", nullable = false)
  private LocalDate visitDate;

  @Column(name = "ip_address", nullable = false, length = 64)
  private String ipAddress;

  @Column(length = 160)
  private String location;

  @Column(name = "visit_count", nullable = false)
  private long visitCount = 1;

  @Column(nullable = false, updatable = false)
  private Instant firstVisitedAt = Instant.now();

  @Column(nullable = false)
  private Instant lastVisitedAt = Instant.now();
}
