package com.fishmart.waterquality;

import com.fishmart.common.ApiResponse;
import com.fishmart.common.BizException;
import com.fishmart.security.AuthUser;
import com.fishmart.user.User;
import com.fishmart.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/water-quality")
public class WaterQualityController {
  private final WaterQualityRepository records;
  private final WaterQualityTargetRepository targets;
  private final UserRepository users;
  public WaterQualityController(WaterQualityRepository records, WaterQualityTargetRepository targets, UserRepository users) { this.records = records; this.targets = targets; this.users = users; }

  @GetMapping
  public ApiResponse<Page<WaterQualityDto>> list(@AuthenticationPrincipal AuthUser authUser,
                                                  @RequestParam(defaultValue = "0") int page) {
    if (authUser == null) throw new BizException("未登录");
    int safePage = Math.max(0, page);
    return ApiResponse.ok(records.findByUserIdOrderByObservedAtDesc(authUser.getId(), PageRequest.of(safePage, 20)).map(WaterQualityDto::from));
  }

  @GetMapping("/{id}")
  public ApiResponse<WaterQualityDto> detail(@PathVariable Long id, @AuthenticationPrincipal AuthUser authUser) {
    if (authUser == null) throw new BizException("未登录");
    WaterQualityRecord item = records.findByIdAndUserId(id, authUser.getId()).orElseThrow(() -> new BizException("水质记录不存在"));
    return ApiResponse.ok(WaterQualityDto.from(item));
  }

  @GetMapping(value = "/export", produces = "text/csv;charset=UTF-8")
  public ResponseEntity<byte[]> export(@AuthenticationPrincipal AuthUser authUser,
                                       @RequestParam(required = false) String pondName) {
    if (authUser == null) throw new BizException("未登录");
    String safePondName = trimToNull(pondName);
    if (safePondName != null && safePondName.length() > 64) throw new BizException("请选择有效水体");
    List<WaterQualityRecord> items = safePondName == null
        ? records.findByUserIdOrderByObservedAtDesc(authUser.getId())
        : records.findByUserIdAndPondNameOrderByObservedAtDesc(authUser.getId(), safePondName);
    StringBuilder csv = new StringBuilder("\uFEFF水体名称,测量时间,水温°C,pH,溶氧mg/L,氨氮mg/L,亚硝酸盐mg/L,备注\n");
    for (WaterQualityRecord item : items) {
      csv.append(csvCell(item.getPondName())).append(',')
          .append(csvCell(item.getObservedAt())).append(',')
          .append(csvCell(item.getTemperature())).append(',')
          .append(csvCell(item.getPh())).append(',')
          .append(csvCell(item.getDissolvedOxygen())).append(',')
          .append(csvCell(item.getAmmonia())).append(',')
          .append(csvCell(item.getNitrite())).append(',')
          .append(csvCell(item.getNotes())).append('\n');
    }
    String filename = "water-quality-" + LocalDate.now(ZoneOffset.UTC) + ".csv";
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
        .body(csv.toString().getBytes(StandardCharsets.UTF_8));
  }

  @GetMapping("/ponds")
  public ApiResponse<List<String>> ponds(@AuthenticationPrincipal AuthUser authUser) {
    if (authUser == null) throw new BizException("未登录");
    return ApiResponse.ok(records.findPondNames(authUser.getId()));
  }

  @GetMapping("/overview")
  public ApiResponse<List<WaterQualityPondOverviewDto>> overview(@AuthenticationPrincipal AuthUser authUser) {
    if (authUser == null) throw new BizException("未登录");
    Map<String, WaterQualityTarget> targetByPond = new LinkedHashMap<>();
    targets.findByUserIdOrderByPondName(authUser.getId()).forEach(target -> targetByPond.put(target.getPondName(), target));
    Map<String, WaterQualityRecord> latestByPond = new LinkedHashMap<>();
    records.findByUserIdOrderByObservedAtDesc(authUser.getId()).forEach(record -> latestByPond.putIfAbsent(record.getPondName(), record));
    Instant now = Instant.now();
    return ApiResponse.ok(latestByPond.values().stream().map(record -> {
      WaterQualityTarget target = targetByPond.get(record.getPondName());
      long ageDays = Math.max(0, ChronoUnit.DAYS.between(record.getObservedAt(), now));
      return new WaterQualityPondOverviewDto(record.getPondName(), WaterQualityDto.from(record), target != null, deviations(record, target), ageDays, ageDays >= 7);
    }).toList());
  }

  @GetMapping("/trend")
  public ApiResponse<List<WaterQualityDto>> trend(@AuthenticationPrincipal AuthUser authUser,
                                                   @RequestParam String pondName,
                                                   @RequestParam(defaultValue = "30") int days) {
    if (authUser == null) throw new BizException("未登录");
    String safePondName = pondName == null ? "" : pondName.trim();
    if (safePondName.isEmpty() || safePondName.length() > 64) throw new BizException("请选择有效水体");
    int safeDays = Math.max(7, Math.min(days, 365));
    List<WaterQualityDto> result = records
        .findByUserIdAndPondNameAndObservedAtAfterOrderByObservedAtAsc(
            authUser.getId(), safePondName, Instant.now().minus(safeDays, ChronoUnit.DAYS), PageRequest.of(0, 366))
        .stream().map(WaterQualityDto::from).toList();
    return ApiResponse.ok(result);
  }

  @GetMapping("/targets")
  public ApiResponse<List<WaterQualityTargetDto>> targets(@AuthenticationPrincipal AuthUser authUser) {
    if (authUser == null) throw new BizException("未登录");
    return ApiResponse.ok(targets.findByUserIdOrderByPondName(authUser.getId()).stream().map(WaterQualityTargetDto::from).toList());
  }

  @PostMapping("/targets")
  public ApiResponse<WaterQualityTargetDto> saveTarget(@AuthenticationPrincipal AuthUser authUser,
                                                        @Valid @RequestBody WaterQualityTargetRequest request) {
    if (authUser == null) throw new BizException("未登录");
    if (!request.hasTarget()) throw new BizException("请至少填写一项参考范围");
    if (!request.hasValidRanges()) throw new BizException("参考范围的最低值不能高于最高值");
    String pondName = request.getPondName().trim();
    WaterQualityTarget item = targets.findByUserIdAndPondName(authUser.getId(), pondName).orElseGet(() -> {
      WaterQualityTarget created = new WaterQualityTarget();
      created.setUser(users.findById(authUser.getId()).orElseThrow(() -> new BizException("用户不存在")));
      created.setPondName(pondName); return created;
    });
    item.setTemperatureMin(request.getTemperatureMin()); item.setTemperatureMax(request.getTemperatureMax());
    item.setPhMin(request.getPhMin()); item.setPhMax(request.getPhMax()); item.setDissolvedOxygenMin(request.getDissolvedOxygenMin());
    item.setAmmoniaMax(request.getAmmoniaMax()); item.setNitriteMax(request.getNitriteMax()); item.setUpdatedAt(Instant.now());
    return ApiResponse.ok("个人参考范围已保存", WaterQualityTargetDto.from(targets.save(item)));
  }

  @DeleteMapping("/targets/{id}")
  public ApiResponse<Void> deleteTarget(@PathVariable Long id, @AuthenticationPrincipal AuthUser authUser) {
    if (authUser == null) throw new BizException("未登录");
    WaterQualityTarget item = targets.findByIdAndUserId(id, authUser.getId()).orElseThrow(() -> new BizException("参考范围不存在"));
    targets.delete(item); return ApiResponse.ok("个人参考范围已删除", null);
  }

  @PostMapping
  public ApiResponse<WaterQualityDto> create(@AuthenticationPrincipal AuthUser authUser,
                                              @Valid @RequestBody WaterQualityRequest request) {
    if (authUser == null) throw new BizException("未登录");
    if (!request.hasMeasurement()) throw new BizException("请至少填写一项水质测量值");
    if (request.getObservedAt().isAfter(Instant.now().plus(5, ChronoUnit.MINUTES))) throw new BizException("观察时间不能晚于当前时间");
    User user = users.findById(authUser.getId()).orElseThrow(() -> new BizException("用户不存在"));
    WaterQualityRecord item = new WaterQualityRecord(); item.setUser(user); apply(item, request);
    return ApiResponse.ok("水质记录已保存", WaterQualityDto.from(records.save(item)));
  }

  @PutMapping("/{id}")
  public ApiResponse<WaterQualityDto> update(@PathVariable Long id,
                                              @AuthenticationPrincipal AuthUser authUser,
                                              @Valid @RequestBody WaterQualityRequest request) {
    if (authUser == null) throw new BizException("未登录");
    if (!request.hasMeasurement()) throw new BizException("请至少填写一项水质测量值");
    if (request.getObservedAt().isAfter(Instant.now().plus(5, ChronoUnit.MINUTES))) throw new BizException("观察时间不能晚于当前时间");
    WaterQualityRecord item = records.findByIdAndUserId(id, authUser.getId()).orElseThrow(() -> new BizException("水质记录不存在"));
    apply(item, request);
    return ApiResponse.ok("水质记录已更新", WaterQualityDto.from(records.save(item)));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal AuthUser authUser) {
    if (authUser == null) throw new BizException("未登录");
    WaterQualityRecord item = records.findByIdAndUserId(id, authUser.getId()).orElseThrow(() -> new BizException("水质记录不存在"));
    records.delete(item); return ApiResponse.ok("水质记录已删除", null);
  }
  private void apply(WaterQualityRecord item, WaterQualityRequest request) {
    item.setPondName(request.getPondName().trim()); item.setTemperature(request.getTemperature()); item.setPh(request.getPh());
    item.setDissolvedOxygen(request.getDissolvedOxygen()); item.setAmmonia(request.getAmmonia()); item.setNitrite(request.getNitrite());
    item.setNotes(trimToNull(request.getNotes())); item.setObservedAt(request.getObservedAt());
  }
  private String trimToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
  private List<String> deviations(WaterQualityRecord record, WaterQualityTarget target) {
    if (target == null) return List.of();
    java.util.ArrayList<String> result = new java.util.ArrayList<>();
    if (below(record.getTemperature(), target.getTemperatureMin()) || above(record.getTemperature(), target.getTemperatureMax())) result.add("水温");
    if (below(record.getPh(), target.getPhMin()) || above(record.getPh(), target.getPhMax())) result.add("pH");
    if (below(record.getDissolvedOxygen(), target.getDissolvedOxygenMin())) result.add("溶氧");
    if (above(record.getAmmonia(), target.getAmmoniaMax())) result.add("氨氮");
    if (above(record.getNitrite(), target.getNitriteMax())) result.add("亚硝酸盐");
    return List.copyOf(result);
  }
  private boolean below(BigDecimal value, BigDecimal minimum) { return value != null && minimum != null && value.compareTo(minimum) < 0; }
  private boolean above(BigDecimal value, BigDecimal maximum) { return value != null && maximum != null && value.compareTo(maximum) > 0; }
  private String csvCell(Object value) {
    String text = value == null ? "" : value instanceof BigDecimal decimal ? decimal.toPlainString() : String.valueOf(value);
    if (!text.isEmpty() && "=+-@".indexOf(text.charAt(0)) >= 0) text = "'" + text;
    return "\"" + text.replace("\"", "\"\"").replace("\r", " ").replace("\n", " ") + "\"";
  }
}
