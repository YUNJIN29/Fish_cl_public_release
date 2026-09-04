package com.fishmart.content;

import com.fishmart.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "posts")
public class Post {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false, length = 200)
  private String title;

  @Lob
  @Column(nullable = false, columnDefinition = "LONGTEXT")
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private ContentStatus status = ContentStatus.APPROVED;

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();
}

