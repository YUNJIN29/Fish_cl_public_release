package com.fishmart.config;

import com.fishmart.user.Role;
import com.fishmart.user.RoleName;
import com.fishmart.user.RoleRepository;
import com.fishmart.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DataInitializerTest {
  @Test
  void productionCreatesRolesButNeverSeedsDefaultAdmin() {
    RoleRepository roles = mock(RoleRepository.class);
    UserRepository users = mock(UserRepository.class);
    PasswordEncoder encoder = mock(PasswordEncoder.class);
    Role userRole = new Role(); userRole.setName(RoleName.USER);
    Role adminRole = new Role(); adminRole.setName(RoleName.ADMIN);
    when(roles.findByName(RoleName.USER)).thenReturn(Optional.of(userRole));
    when(roles.findByName(RoleName.ADMIN)).thenReturn(Optional.of(adminRole));

    new DataInitializer(roles, users, encoder, "admin", "").run();

    verify(users, never()).findByUsername("admin");
    verify(users, never()).save(org.mockito.ArgumentMatchers.any());
  }
}
