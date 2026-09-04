package com.fishmart.content;

import com.fishmart.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "comments")
public class Comment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private TargetType targetType;

  @Column(nullable = false)
  private Long targetId;

  @Column(nullable = false, length = 1000)
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private ContentStatus status = ContentStatus.APPROVED;

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();
}

