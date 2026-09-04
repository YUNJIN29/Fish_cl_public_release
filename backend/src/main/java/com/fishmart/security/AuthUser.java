package com.fishmart.security;

import com.fishmart.user.RoleName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class AuthUser {
  private Long id;
  private String username;
  private Set<RoleName> roles;
}

