package com.fishmart.visit;

import com.fishmart.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.util.List;

@RestController
@RequestMapping("/api/visits")
public class VisitStatsController {
  private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
  private final DailyVisitRepository repository;
  private final IpLocationService locationService;

  public VisitStatsController(DailyVisitRepository repository, IpLocationService locationService) { this.repository = repository; this.locationService = locationService; }

  public record VisitStats(long todayVisitors, long monthVisitors, long thisIpVisits) {}
  public record RankingItem(String ip, String location, long visits) {}

  @RequestMapping(value = "/track", method = {RequestMethod.POST, RequestMethod.GET})
  @Transactional
  public ApiResponse<VisitStats> track(HttpServletRequest request) {
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
    return ApiResponse.ok(stats(today, visit.getVisitCount()));
  }

  @GetMapping("/stats")
  public ApiResponse<VisitStats> stats() {
    LocalDate today = LocalDate.now(ZONE);
    return ApiResponse.ok(stats(today, 0));
  }

  @GetMapping("/monthly-ranking")
  public ApiResponse<List<RankingItem>> monthlyRanking() {
    LocalDate today = LocalDate.now(ZONE);
    List<RankingItem> result = repository.monthlyRanking(today.withDayOfMonth(1), today).stream()
        .limit(10)
        .map(row -> new RankingItem(maskIp(String.valueOf(row[0])), row[1] == null ? "未知地区" : String.valueOf(row[1]), ((Number) row[2]).longValue()))
        .toList();
    return ApiResponse.ok(result);
  }

  private VisitStats stats(LocalDate today, long thisIpVisits) {
    LocalDate monthStart = today.withDayOfMonth(1);
    return new VisitStats(repository.countByVisitDate(today), repository.countUniqueIpsBetween(monthStart, today), thisIpVisits);
  }

  private String clientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
    String real = request.getHeader("X-Real-IP");
    return real != null && !real.isBlank() ? real.trim() : request.getRemoteAddr();
  }

  private String maskIp(String ip) {
    if (ip.contains(".")) {
      String[] parts = ip.split("\\.");
      return parts.length == 4 ? parts[0] + "." + parts[1] + ".*.*" : "*.*";
    }
    String[] parts = ip.split(":", -1);
    if (parts.length > 2) {
      parts[parts.length - 1] = "*";
      parts[parts.length - 2] = "*";
      return String.join(":", parts);
    }
    return "*:*";
  }
}
