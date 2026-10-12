package com.fishmart.task;

import com.fishmart.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter @Setter @Entity
@Table(name = "aquaculture_tasks")
public class AquacultureTask {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private User user;
  @Column(nullable = false, length = 100) private String title;
  @Column(nullable = false, length = 24) private String category;
  @Column(length = 64) private String pondName;
  @Column(nullable = false) private Instant dueAt;
  @Column(length = 500) private String notes;
  private Integer repeatIntervalDays;
  @Column(nullable = false) private boolean nextTaskCreated = false;
  private Instant completedAt;
  @Column(nullable = false, updatable = false) private Instant createdAt = Instant.now();
  @Column(nullable = false) private Instant updatedAt = Instant.now();
}
