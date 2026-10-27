package com.fishmart.guestbook;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("guestbookManagerGuard")
public class GuestbookManagerGuard {
  public boolean canManage(Authentication authentication) {
    return authentication != null && authentication.isAuthenticated()
        && authentication.getAuthorities().stream()
            .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
  }
}
