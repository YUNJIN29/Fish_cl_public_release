package com.fishmart.auth;

import com.fishmart.auth.dto.AuthResponse;
import com.fishmart.auth.dto.ChangePasswordRequest;
import com.fishmart.auth.dto.LoginRequest;
import com.fishmart.auth.dto.RegisterRequest;
import com.fishmart.auth.dto.UpdateProfileRequest;
import com.fishmart.common.ApiResponse;
import com.fishmart.security.AuthUser;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
    return ApiResponse.ok(authService.register(req));
  }

  @PostMapping("/login")
  public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
    return ApiResponse.ok(authService.login(req));
  }

  @GetMapping("/me")
  public ApiResponse<AuthResponse.UserProfile> me(Authentication authentication) {
    AuthUser principal = (AuthUser) authentication.getPrincipal();
    return ApiResponse.ok(authService.me(principal).getMe());
  }

  @PutMapping("/me")
  public ApiResponse<AuthResponse.UserProfile> updateMe(Authentication authentication,
                                                        @Valid @RequestBody UpdateProfileRequest req) {
    AuthUser principal = (AuthUser) authentication.getPrincipal();
    return ApiResponse.ok(authService.updateProfile(principal, req));
  }

  @PostMapping("/change-password")
  public ApiResponse<Void> changePassword(Authentication authentication,
                                          @Valid @RequestBody ChangePasswordRequest req) {
    AuthUser principal = (AuthUser) authentication.getPrincipal();
    authService.changePassword(principal, req);
    return ApiResponse.ok("密码已更新", null);
  }
}

