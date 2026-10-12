package com.fishmart.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fishmart.operations.OperationalMetrics;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class RequestRateLimitFilterTest {
  @Test
  void limitsRepeatedLoginAttempts() throws Exception {
    RequestRateLimitFilter filter = new RequestRateLimitFilter(new ObjectMapper(), new OperationalMetrics());
    MockHttpServletResponse lastResponse = null;
    for (int i = 0; i < 13; i++) {
      MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
      request.setRemoteAddr("203.0.113.10");
      lastResponse = new MockHttpServletResponse();
      filter.doFilter(request, lastResponse, new MockFilterChain());
    }
    assertThat(lastResponse).isNotNull();
    assertThat(lastResponse.getStatus()).isEqualTo(429);
    assertThat(lastResponse.getContentAsString()).contains("RATE_LIMITED");
  }

  @Test
  void limitsGuestbookSubmissions() throws Exception {
    RequestRateLimitFilter filter = new RequestRateLimitFilter(new ObjectMapper(), new OperationalMetrics());
    MockHttpServletResponse lastResponse = null;
    for (int i = 0; i < 6; i++) {
      MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/guestbook");
      request.setRemoteAddr("203.0.113.20");
      lastResponse = new MockHttpServletResponse();
      filter.doFilter(request, lastResponse, new MockFilterChain());
    }
    assertThat(lastResponse).isNotNull();
    assertThat(lastResponse.getStatus()).isEqualTo(429);
  }
}
