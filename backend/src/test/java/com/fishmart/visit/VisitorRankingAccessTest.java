package com.fishmart.visit;

import com.fishmart.guestbook.GuestbookManagerGuard;
import com.fishmart.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class VisitorRankingAccessTest {
  @Configuration
  @EnableMethodSecurity
  static class Config {}

  @Test void rankingRequiresOwnerOrAdministrator() {
    var repository = mock(DailyVisitRepository.class);
    LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
    when(repository.monthlyRanking(today.withDayOfMonth(1), today))
        .thenReturn(java.util.Collections.singletonList(new Object[]{"203.0.113.5", "测试地区", 12L}));
    try (var context = new AnnotationConfigApplicationContext()) {
      context.register(Config.class);
      context.registerBean("guestbookManagerGuard", GuestbookManagerGuard.class,
          GuestbookManagerGuard::new);
      context.registerBean(VisitStatsController.class,
          () -> new VisitStatsController(repository, mock(IpLocationService.class)));
      context.refresh();
      var controller = context.getBean(VisitStatsController.class);
      for (String username : List.of("manager", "administrator")) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
            username, "", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        var response = new MockHttpServletResponse();
        assertThat(controller.ranking(response).getData()).containsExactly(
            new VisitStatsController.VisitorRanking("203.0.113.5", "测试地区", 12));
        assertThat(response.getHeader("Cache-Control")).isEqualTo("no-store");
      }
      clearInvocations(repository);
      SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
          "visitor", "", List.of(new SimpleGrantedAuthority("ROLE_USER"))));
      assertThatThrownBy(() -> controller.ranking(new MockHttpServletResponse())).isInstanceOf(AccessDeniedException.class);
      verifyNoInteractions(repository);
    } finally {
      SecurityContextHolder.clearContext();
    }
  }
}
