package com.fishmart.operations;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class OperationalMetricsFilter extends OncePerRequestFilter {
  private final OperationalMetrics metrics;
  public OperationalMetricsFilter(OperationalMetrics metrics) { this.metrics = metrics; }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    long started = System.nanoTime();
    try {
      chain.doFilter(request, response);
    } finally {
      boolean ok = response.getStatus() < 400;
      String uri = request.getRequestURI();
      if ("POST".equalsIgnoreCase(request.getMethod()) && "/api/diagnose".equals(uri)) {
        metrics.diagnose(ok, (System.nanoTime() - started) / 1_000_000);
      }
      if (!ok && "POST".equalsIgnoreCase(request.getMethod()) &&
          (uri.startsWith("/api/upload") || uri.startsWith("/api/print/upload"))) metrics.uploadFailure();
    }
  }
}
