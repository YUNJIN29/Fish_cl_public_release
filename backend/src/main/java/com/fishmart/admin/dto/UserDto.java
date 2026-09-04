package com.fishmart.admin.dto;

import com.fishmart.user.Role;
import com.fishmart.user.RoleName;
import com.fishmart.user.User;
import com.fishmart.user.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class UserDto {
  private Long id;
  private String username;
  private String nickname;
  private String avatarUrl;
  private UserStatus status;
  private Set<RoleName> roles;
  private Instant createdAt;

  public static UserDto from(User u) {
    Set<RoleName> roles = u.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
    return new UserDto(u.getId(), u.getUsername(), u.getNickname(), u.getAvatarUrl(), u.getStatus(), roles, u.getCreatedAt());
  }
}

