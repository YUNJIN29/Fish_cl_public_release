package com.fishmart.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 64)
  private String username;

  @Column(nullable = false, length = 255)
  private String passwordHash;

  @Column(nullable = false, length = 64)
  private String nickname;

  @Column(length = 512)
  private String avatarUrl;

  @Column(length = 128)
  private String email;

  @Column(length = 32)
  private String phone;

  @Column(length = 128)
  private String farmName;

  @Column(length = 128)
  private String farmLocation;

  @Column(length = 128)
  private String mainSpecies;

  @Column(length = 512)
  private String bio;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private UserStatus status = UserStatus.ACTIVE;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id")
  )
  private Set<Role> roles = new HashSet<>();

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  @Column(nullable = false)
  private Instant updatedAt = Instant.now();

  @PreUpdate
  public void touch() {
    updatedAt = Instant.now();
  }
}

