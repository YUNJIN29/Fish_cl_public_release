package com.fishmart.content;

import com.fishmart.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "favorites", uniqueConstraints = {
    @UniqueConstraint(name = "uk_favorites", columnNames = {"user_id", "target_type", "target_id"})
})
public class FavoriteEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(name = "target_type", nullable = false, length = 16)
  private TargetType targetType;

  @Column(name = "target_id", nullable = false)
  private Long targetId;

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();
}

