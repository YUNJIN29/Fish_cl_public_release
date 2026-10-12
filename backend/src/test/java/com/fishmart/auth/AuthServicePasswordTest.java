package com.fishmart.auth;

import com.fishmart.auth.dto.ChangePasswordRequest;
import com.fishmart.common.BizException;
import com.fishmart.operations.OperationalMetrics;
import com.fishmart.security.AuthUser;
import com.fishmart.security.JwtService;
import com.fishmart.user.RoleName;
import com.fishmart.user.RoleRepository;
import com.fishmart.user.User;
import com.fishmart.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServicePasswordTest {
  @Mock UserRepository users;
  @Mock RoleRepository roles;
  @Mock PasswordEncoder encoder;
  @Mock JwtService jwt;
  @Mock OperationalMetrics metrics;

  @Test
  void changesPasswordOnlyAfterVerifyingCurrentPassword() {
    AuthService service = new AuthService(users, roles, encoder, jwt, metrics);
    User user = new User(); user.setId(7L); user.setPasswordHash("old-hash");
    when(users.findById(7L)).thenReturn(Optional.of(user));
    when(encoder.matches("OldPass123", "old-hash")).thenReturn(true);
    when(encoder.matches("NewPass456", "old-hash")).thenReturn(false);
    when(encoder.encode("NewPass456")).thenReturn("new-hash");
    ChangePasswordRequest request = new ChangePasswordRequest(); request.setCurrentPassword("OldPass123"); request.setNewPassword("NewPass456");

    service.changePassword(new AuthUser(7L, "owner", Set.of(RoleName.USER)), request);

    verify(users).save(user);
    org.assertj.core.api.Assertions.assertThat(user.getPasswordHash()).isEqualTo("new-hash");
  }

  @Test
  void rejectsIncorrectCurrentPassword() {
    AuthService service = new AuthService(users, roles, encoder, jwt, metrics);
    User user = new User(); user.setId(7L); user.setPasswordHash("old-hash");
    when(users.findById(7L)).thenReturn(Optional.of(user));
    when(encoder.matches("wrong", "old-hash")).thenReturn(false);
    ChangePasswordRequest request = new ChangePasswordRequest(); request.setCurrentPassword("wrong"); request.setNewPassword("NewPass456");

    assertThatThrownBy(() -> service.changePassword(new AuthUser(7L, "owner", Set.of(RoleName.USER)), request))
        .isInstanceOf(BizException.class).hasMessage("当前密码错误");
    verify(users, never()).save(user);
  }
}
