package com.fishmart.guestbook;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class GuestbookManagerGuardTest {
  @Test void onlyAdministratorCanManage() {
    var guard = new GuestbookManagerGuard();
    assertThat(guard.canManage(null)).isFalse();
    for (String name : List.of("owner", "admin", "visitor")) {
      var ordinary = new UsernamePasswordAuthenticationToken(name, "",
          List.of(new SimpleGrantedAuthority("ROLE_USER")));
      assertThat(guard.canManage(ordinary)).isFalse();
    }
    var administrator = new UsernamePasswordAuthenticationToken("manager", "",
        List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    assertThat(guard.canManage(administrator)).isTrue();
  }
}
