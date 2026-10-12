package com.fishmart.guestbook;

import com.fishmart.content.ContentStatus;
import com.fishmart.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "guestbook_messages")
public class GuestbookMessage {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "author_name", nullable = false, length = 32)
  private String authorName;

  @Column(nullable = false, length = 500)
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private ContentStatus status = ContentStatus.PENDING;

  @Column(length = 500)
  private String adminReply;

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  private Instant reviewedAt;
}
