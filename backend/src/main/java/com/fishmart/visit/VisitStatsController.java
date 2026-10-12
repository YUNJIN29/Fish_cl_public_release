package com.fishmart.visit;

import com.fishmart.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.*;

@RestController
@RequestMapping("/api/visits")
public class VisitStatsController {
  private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
  private final DailyVisitRepository repository;
  private final IpLocationService locationService;

  public VisitStatsController(DailyVisitRepository repository, IpLocationService locationService) { this.repository = repository; this.locationService = locationService; }

  public record VisitSummary(long todayVisitors, long monthVisitors, long totalVisitors) {}

  @PostMapping("/track")
  @Transactional
  public ApiResponse<VisitSummary> track(HttpServletRequest request) {
    LocalDate today = LocalDate.now(ZONE);
    String ip = clientIp(request);
    DailyVisit visit = repository.findByVisitDateAndIpAddress(today, ip).orElseGet(() -> {
      DailyVisit item = new DailyVisit();
      item.setVisitDate(today);
      item.setIpAddress(ip);
      item.setVisitCount(0);
      return item;
    });
    visit.setVisitCount(visit.getVisitCount() + 1);
    if (visit.getLocation() == null || visit.getLocation().isBlank()) visit.setLocation(locationService.resolve(ip));
    visit.setLastVisitedAt(Instant.now());
    repository.saveAndFlush(visit);
    return ApiResponse.ok(summary(today));
  }

  @GetMapping("/summary")
  public ApiResponse<VisitSummary> summary() {
    LocalDate today = LocalDate.now(ZONE);
    return ApiResponse.ok(summary(today));
  }

  private VisitSummary summary(LocalDate today) {
    LocalDate monthStart = today.withDayOfMonth(1);
    return new VisitSummary(
        repository.countByVisitDate(today),
        repository.countUniqueIpsBetween(monthStart, today),
        repository.countAllUniqueIps()
    );
  }

  private String clientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
    String real = request.getHeader("X-Real-IP");
    return real != null && !real.isBlank() ? real.trim() : request.getRemoteAddr();
  }

}
