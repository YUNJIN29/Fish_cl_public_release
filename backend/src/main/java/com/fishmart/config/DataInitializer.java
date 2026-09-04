package com.fishmart.config;

import com.fishmart.user.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
  private final RoleRepository roleRepo;
  private final UserRepository userRepo;
  private final PasswordEncoder passwordEncoder;
  private final String adminUsername;
  private final String adminPassword;

  public DataInitializer(
      RoleRepository roleRepo,
      UserRepository userRepo,
      PasswordEncoder passwordEncoder,
      @Value("${app.admin.username:admin}") String adminUsername,
      @Value("${app.admin.password:}") String adminPassword
  ) {
    this.roleRepo = roleRepo;
    this.userRepo = userRepo;
    this.passwordEncoder = passwordEncoder;
    this.adminUsername = adminUsername;
    this.adminPassword = adminPassword;
  }

  @Override
  public void run(String... args) {
    Role userRole = roleRepo.findByName(RoleName.USER).orElseGet(() -> {
      Role r = new Role();
      r.setName(RoleName.USER);
      return roleRepo.save(r);
    });
    Role adminRole = roleRepo.findByName(RoleName.ADMIN).orElseGet(() -> {
      Role r = new Role();
      r.setName(RoleName.ADMIN);
      return roleRepo.save(r);
    });

    if (adminPassword == null || adminPassword.isBlank()) {
      return;
    }

    userRepo.findByUsername(adminUsername).orElseGet(() -> {
      User u = new User();
      u.setUsername(adminUsername);
      u.setNickname("管理员");
      u.setPasswordHash(passwordEncoder.encode(adminPassword));
      u.getRoles().add(userRole);
      u.getRoles().add(adminRole);
      return userRepo.save(u);
    });
  }
}

