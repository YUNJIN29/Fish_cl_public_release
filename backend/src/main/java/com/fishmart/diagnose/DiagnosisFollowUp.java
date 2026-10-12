package com.fishmart.diagnose;

import com.fishmart.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "diagnosis_follow_ups")
public class DiagnosisFollowUp {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "diagnose_record_id", nullable = false)
  private DiagnoseRecord diagnoseRecord;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false, length = 16)
  private String conditionStatus;

  @Column(nullable = false, length = 16)
  private String appetiteStatus;

  @Column(nullable = false)
  private Integer mortalityCount = 0;

  private String waterQuality;

  @Column(length = 1000)
  private String notes;

  @Column(nullable = false)
  private Instant observedAt;

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();
}
