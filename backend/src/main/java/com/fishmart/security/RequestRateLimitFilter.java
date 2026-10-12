package com.fishmart.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fishmart.common.ApiResponse;
import com.fishmart.operations.OperationalMetrics;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RequestRateLimitFilter extends OncePerRequestFilter {
  private static final long WINDOW_SECONDS = 60;
  private static final Map<String, Integer> LIMITS = Map.of(
      "/api/auth/login", 12,
      "/api/auth/register", 6,
      "/api/auth/change-password", 6,
      "/api/diagnose", 8,
      "/api/upload", 20,
      "/api/print/upload", 10,
      "/api/game-scores", 30,
      "/api/guestbook", 5,
      "/api/water-quality", 30,
      "/api/visits/track", 20
  );

  private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();
  private final ObjectMapper objectMapper;
  private final OperationalMetrics metrics;

  public RequestRateLimitFilter(ObjectMapper objectMapper, OperationalMetrics metrics) {
    this.objectMapper = objectMapper;
    this.metrics = metrics;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    boolean limitedMethod = "POST".equalsIgnoreCase(request.getMethod()) || "PUT".equalsIgnoreCase(request.getMethod()) || "PATCH".equalsIgnoreCase(request.getMethod());
    if (!limitedMethod) {
      filterChain.doFilter(request, response);
      return;
    }

    Integer limit = LIMITS.get(request.getRequestURI());
    if (limit == null && request.getRequestURI().startsWith("/api/water-quality/targets")) limit = 20;
    if (limit == null && "PUT".equalsIgnoreCase(request.getMethod()) && request.getRequestURI().startsWith("/api/water-quality/")) limit = 30;
    if (limit == null && request.getRequestURI().startsWith("/api/tasks")) limit = 30;
    if (limit == null) {
      filterChain.doFilter(request, response);
      return;
    }

    long window = Instant.now().getEpochSecond() / WINDOW_SECONDS;
    String key = clientIp(request) + ':' + request.getRequestURI();
    Counter counter = counters.compute(key, (ignored, current) ->
        current == null || current.window != window ? new Counter(window, 1) : new Counter(window, current.count + 1));

    if (counter.count > limit) {
      metrics.rateLimited();
      response.setStatus(429);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.setCharacterEncoding("UTF-8");
      response.setHeader("Retry-After", String.valueOf(WINDOW_SECONDS));
      objectMapper.writeValue(response.getWriter(), ApiResponse.error("RATE_LIMITED", "请求过于频繁，请稍后再试"));
      return;
    }
    if (counters.size() > 10_000) counters.entrySet().removeIf(entry -> entry.getValue().window < window - 2);
    filterChain.doFilter(request, response);
  }

  private String clientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
    String real = request.getHeader("X-Real-IP");
    return real == null || real.isBlank() ? request.getRemoteAddr() : real.trim();
  }

  private record Counter(long window, int count) {}
}
