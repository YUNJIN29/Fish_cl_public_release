package com.fishmart.admin;

import com.fishmart.common.ApiResponse;
import com.fishmart.config.AppProperties;
import com.fishmart.content.CommentRepository;
import com.fishmart.content.ContentStatus;
import com.fishmart.content.PostRepository;
import com.fishmart.operations.OperationalMetrics;
import com.fishmart.visit.DailyVisitRepository;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/operations")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOperationsController {
  private final DailyVisitRepository visits;
  private final OperationalMetrics metrics;
  private final HealthEndpoint health;
  private final DataSource dataSource;
  private final AppProperties props;
  private final PostRepository posts;
  private final CommentRepository comments;

  public AdminOperationsController(DailyVisitRepository visits, OperationalMetrics metrics, HealthEndpoint health,
                                   DataSource dataSource, AppProperties props, PostRepository posts, CommentRepository comments) {
    this.visits = visits; this.metrics = metrics; this.health = health; this.dataSource = dataSource;
    this.props = props; this.posts = posts; this.comments = comments;
  }

  @GetMapping("/overview")
  public ApiResponse<Map<String, Object>> overview() {
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("status", health.health().getStatus().getCode());
    result.put("database", databaseStatus());
    result.put("disk", diskStatus());
    result.put("aiConfigured", props.getAi().getGlm4BaseUrl() != null && !props.getAi().getGlm4BaseUrl().isBlank());
    long pending = posts.search(null, ContentStatus.PENDING, PageRequest.of(0, 1)).getTotalElements()
        + comments.findByStatusOrderByIdDesc(ContentStatus.PENDING, PageRequest.of(0, 1)).getTotalElements();
    result.put("pendingContent", pending);
    result.put("metrics", metrics.snapshot());
    return ApiResponse.ok(result);
  }

  @GetMapping("/traffic")
  public ApiResponse<List<Map<String, Object>>> traffic() {
    LocalDate today = LocalDate.now();
    Map<LocalDate, Object[]> values = new LinkedHashMap<>();
    visits.trafficBetween(today.minusDays(6), today).forEach(row -> values.put((LocalDate) row[0], row));
    return ApiResponse.ok(today.minusDays(6).datesUntil(today.plusDays(1)).map(date -> {
      Object[] row = values.get(date);
      return Map.<String, Object>of("date", date.toString(), "views", row == null ? 0 : row[1], "visitors", row == null ? 0 : row[2]);
    }).toList());
  }

  @GetMapping("/diagnostics")
  public ApiResponse<Map<String, Object>> diagnostics() { return ApiResponse.ok(metrics.snapshot()); }

  private String databaseStatus() {
    try (var connection = dataSource.getConnection()) { return connection.isValid(2) ? "UP" : "DOWN"; }
    catch (Exception ignored) { return "DOWN"; }
  }

  private Map<String, Object> diskStatus() {
    try {
      FileStore store = Files.getFileStore(Path.of(".").toAbsolutePath());
      long usable = store.getUsableSpace();
      return Map.of("status", usable > 512L * 1024 * 1024 ? "UP" : "LOW", "usableBytes", usable, "totalBytes", store.getTotalSpace());
    } catch (Exception ignored) { return Map.of("status", "UNKNOWN"); }
  }
}
