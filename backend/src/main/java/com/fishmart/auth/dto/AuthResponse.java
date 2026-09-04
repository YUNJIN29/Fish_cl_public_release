package com.fishmart.auth.dto;

import com.fishmart.user.RoleName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.Set;

@Data
@AllArgsConstructor
public class AuthResponse {
  private String accessToken;
  private UserProfile me;

  @Data
  @AllArgsConstructor
  public static class UserProfile {
    private Long id;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String email;
    private String phone;
    private String farmName;
    private String farmLocation;
    private String mainSpecies;
    private String bio;
    private Instant createdAt;
    private Set<RoleName> roles;
  }
}

