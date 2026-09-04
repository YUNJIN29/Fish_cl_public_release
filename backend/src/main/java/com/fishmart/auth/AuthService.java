package com.fishmart.auth;

import com.fishmart.auth.dto.AuthResponse;
import com.fishmart.auth.dto.LoginRequest;
import com.fishmart.auth.dto.RegisterRequest;
import com.fishmart.auth.dto.UpdateProfileRequest;
import com.fishmart.common.BizException;
import com.fishmart.security.AuthUser;
import com.fishmart.security.JwtService;
import com.fishmart.user.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {
  private final UserRepository userRepo;
  private final RoleRepository roleRepo;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthService(UserRepository userRepo, RoleRepository roleRepo, PasswordEncoder passwordEncoder, JwtService jwtService) {
    this.userRepo = userRepo;
    this.roleRepo = roleRepo;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  @Transactional
  public AuthResponse register(RegisterRequest req) {
    if (userRepo.existsByUsername(req.getUsername())) {
      throw new BizException("用户名已存在");
    }
    Role userRole = roleRepo.findByName(RoleName.USER).orElseThrow(() -> new BizException("系统缺少 USER 角色"));

    User u = new User();
    u.setUsername(req.getUsername());
    u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
    u.setNickname(req.getNickname());
    u.getRoles().add(userRole);
    u = userRepo.save(u);

    return issueToken(u);
  }

  public AuthResponse login(LoginRequest req) {
    User u = userRepo.findByUsername(req.getUsername()).orElseThrow(() -> new BizException("账号或密码错误"));
    if (u.getStatus() == UserStatus.BANNED) {
      throw new BizException("账号已被封禁");
    }
    if (!passwordEncoder.matches(req.getPassword(), u.getPasswordHash())) {
      throw new BizException("账号或密码错误");
    }
    return issueToken(u);
  }

  public AuthResponse.UserProfile toProfile(User u) {
    Set<RoleName> roles = u.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
    return new AuthResponse.UserProfile(
        u.getId(),
        u.getUsername(),
        u.getNickname(),
        u.getAvatarUrl(),
        u.getEmail(),
        u.getPhone(),
        u.getFarmName(),
        u.getFarmLocation(),
        u.getMainSpecies(),
        u.getBio(),
        u.getCreatedAt(),
        roles
    );
  }

  public AuthResponse me(AuthUser principal) {
    User u = userRepo.findById(principal.getId()).orElseThrow(() -> new BizException("用户不存在"));
    return new AuthResponse(null, toProfile(u));
  }

  @Transactional
  public AuthResponse.UserProfile updateProfile(AuthUser principal, UpdateProfileRequest req) {
    User u = userRepo.findById(principal.getId()).orElseThrow(() -> new BizException("用户不存在"));
    if (req.getNickname() != null && !req.getNickname().isBlank()) u.setNickname(req.getNickname().trim());
    if (req.getAvatarUrl() != null) u.setAvatarUrl(clean(req.getAvatarUrl()));
    if (req.getEmail() != null) u.setEmail(clean(req.getEmail()));
    if (req.getPhone() != null) u.setPhone(clean(req.getPhone()));
    if (req.getFarmName() != null) u.setFarmName(clean(req.getFarmName()));
    if (req.getFarmLocation() != null) u.setFarmLocation(clean(req.getFarmLocation()));
    if (req.getMainSpecies() != null) u.setMainSpecies(clean(req.getMainSpecies()));
    if (req.getBio() != null) u.setBio(clean(req.getBio()));
    return toProfile(userRepo.save(u));
  }

  private String clean(String value) {
    String trimmed = value.trim();
    return trimmed.isBlank() ? null : trimmed;
  }

  private AuthResponse issueToken(User u) {
    Set<RoleName> roles = u.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
    String token = jwtService.createAccessToken(u.getId(), u.getUsername(), roles);
    return new AuthResponse(token, toProfile(u));
  }
}

