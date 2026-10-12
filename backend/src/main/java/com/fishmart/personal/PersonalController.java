package com.fishmart.personal;

import com.fishmart.common.ApiResponse;
import com.fishmart.content.FavoriteRepository;
import com.fishmart.content.PostRepository;
import com.fishmart.content.dto.PostDto;
import com.fishmart.diagnose.DiagnoseRecordRepository;
import com.fishmart.diagnose.DiagnosisFollowUp;
import com.fishmart.diagnose.DiagnosisFollowUpRepository;
import com.fishmart.diagnose.dto.DiagnoseRecordDto;
import com.fishmart.order.OrderRepository;
import com.fishmart.order.dto.OrderDto;
import com.fishmart.personal.dto.PersonalOverviewDto;
import com.fishmart.personal.dto.DiagnosisAttentionDto;
import com.fishmart.personal.dto.WaterQualityAttentionDto;
import com.fishmart.security.AuthUser;
import com.fishmart.user.User;
import com.fishmart.user.UserRepository;
import com.fishmart.task.AquacultureTaskDto;
import com.fishmart.task.AquacultureTaskRepository;
import com.fishmart.waterquality.WaterQualityRecord;
import com.fishmart.waterquality.WaterQualityRepository;
import com.fishmart.waterquality.WaterQualityTarget;
import com.fishmart.waterquality.WaterQualityTargetRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Stream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.function.BiPredicate;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api/personal")
public class PersonalController {
  private final UserRepository userRepo;
  private final DiagnoseRecordRepository diagnoseRepo;
  private final OrderRepository orderRepo;
  private final PostRepository postRepo;
  private final FavoriteRepository favoriteRepo;
  private final DiagnosisFollowUpRepository followUpRepo;
  private final WaterQualityRepository waterQualityRepo;
  private final WaterQualityTargetRepository waterTargetRepo;
  private final AquacultureTaskRepository taskRepo;

  public PersonalController(UserRepository userRepo,
                            DiagnoseRecordRepository diagnoseRepo,
                            OrderRepository orderRepo,
                            PostRepository postRepo,
                            FavoriteRepository favoriteRepo,
                            DiagnosisFollowUpRepository followUpRepo,
                            WaterQualityRepository waterQualityRepo,
                            WaterQualityTargetRepository waterTargetRepo,
                            AquacultureTaskRepository taskRepo) {
    this.userRepo = userRepo;
    this.diagnoseRepo = diagnoseRepo;
    this.orderRepo = orderRepo;
    this.postRepo = postRepo;
    this.favoriteRepo = favoriteRepo;
    this.followUpRepo = followUpRepo;
    this.waterQualityRepo = waterQualityRepo;
    this.waterTargetRepo = waterTargetRepo;
    this.taskRepo = taskRepo;
  }

  @GetMapping("/overview")
  public ApiResponse<PersonalOverviewDto> overview(@AuthenticationPrincipal AuthUser authUser) {
    User user = userRepo.findById(authUser.getId()).orElseThrow();
    PageRequest recentByTime = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "createdAt"));
    PageRequest recentOrders = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "id"));

    PersonalOverviewDto.Stats stats = new PersonalOverviewDto.Stats(
        diagnoseRepo.countByUserId(user.getId()),
        orderRepo.countByUserId(user.getId()),
        postRepo.countByUserId(user.getId()),
        favoriteRepo.countByUserId(user.getId()),
        taskRepo.countByUserIdAndCompletedAtIsNull(user.getId())
    );

    return ApiResponse.ok(new PersonalOverviewDto(
        profileCompletion(user),
        stats,
        diagnoseRepo.findByUserIdOrderByCreatedAtDesc(user.getId(), recentByTime).map(DiagnoseRecordDto::from).toList(),
        orderRepo.findByUserIdOrderByIdDesc(user.getId(), recentOrders).map(OrderDto::from).toList(),
        postRepo.findByUserIdOrderByCreatedAtDesc(user.getId(), recentByTime).map(post -> PostDto.from(post, false)).toList(),
        attentionFrom(followUpRepo.findByUserIdOrderByObservedAtDesc(user.getId(), PageRequest.of(0, 100)).getContent()),
        waterAttentionFrom(
            waterQualityRepo.findByUserIdOrderByObservedAtDesc(user.getId(), PageRequest.of(0, 200)).getContent(),
            waterTargetRepo.findByUserIdOrderByPondName(user.getId())),
        taskRepo.findByUserIdAndCompletedAtIsNullOrderByDueAtAsc(user.getId(), PageRequest.of(0, 5)).map(AquacultureTaskDto::from).toList()
    ));
  }

  static List<DiagnosisAttentionDto> attentionFrom(List<DiagnosisFollowUp> observations) {
    LinkedHashMap<Long, DiagnosisFollowUp> latestByDiagnosis = new LinkedHashMap<>();
    for (DiagnosisFollowUp item : observations) {
      latestByDiagnosis.putIfAbsent(item.getDiagnoseRecord().getId(), item);
    }
    return latestByDiagnosis.values().stream()
        .filter(item -> !"RECOVERED".equals(item.getConditionStatus()))
        .filter(item -> "WORSENING".equals(item.getConditionStatus()) || item.getMortalityCount() > 0)
        .limit(3)
        .map(DiagnosisAttentionDto::from)
        .toList();
  }

  static List<WaterQualityAttentionDto> waterAttentionFrom(List<WaterQualityRecord> records, List<WaterQualityTarget> targets) {
    Map<String, WaterQualityTarget> targetByPond = targets.stream().collect(java.util.stream.Collectors.toMap(WaterQualityTarget::getPondName, item -> item));
    LinkedHashMap<String, WaterQualityRecord> latestByPond = new LinkedHashMap<>();
    for (WaterQualityRecord record : records) latestByPond.putIfAbsent(record.getPondName(), record);
    Instant now = Instant.now();
    return latestByPond.values().stream().map(record -> {
      WaterQualityTarget target = targetByPond.get(record.getPondName());
      List<String> deviations = new java.util.ArrayList<>();
      if (target != null) {
        if (outside(record.getTemperature(), target.getTemperatureMin(), (value, limit) -> value.compareTo(limit) < 0)
            || outside(record.getTemperature(), target.getTemperatureMax(), (value, limit) -> value.compareTo(limit) > 0)) deviations.add("水温");
        if (outside(record.getPh(), target.getPhMin(), (value, limit) -> value.compareTo(limit) < 0)
            || outside(record.getPh(), target.getPhMax(), (value, limit) -> value.compareTo(limit) > 0)) deviations.add("pH");
        if (outside(record.getDissolvedOxygen(), target.getDissolvedOxygenMin(), (value, limit) -> value.compareTo(limit) < 0)) deviations.add("溶氧");
        if (outside(record.getAmmonia(), target.getAmmoniaMax(), (value, limit) -> value.compareTo(limit) > 0)) deviations.add("氨氮");
        if (outside(record.getNitrite(), target.getNitriteMax(), (value, limit) -> value.compareTo(limit) > 0)) deviations.add("亚硝酸盐");
      }
      long ageDays = Math.max(0, ChronoUnit.DAYS.between(record.getObservedAt(), now));
      boolean stale = ageDays >= 7;
      return deviations.isEmpty() && !stale ? null : WaterQualityAttentionDto.from(record, deviations, ageDays, stale);
    }).filter(java.util.Objects::nonNull).limit(3).toList();
  }

  private static boolean outside(BigDecimal value, BigDecimal limit, BiPredicate<BigDecimal, BigDecimal> predicate) {
    return value != null && limit != null && predicate.test(value, limit);
  }

  private int profileCompletion(User user) {
    long filled = Stream.of(
        user.getNickname(),
        user.getAvatarUrl(),
        user.getEmail(),
        user.getPhone(),
        user.getFarmName(),
        user.getFarmLocation(),
        user.getMainSpecies(),
        user.getBio()
    ).filter(value -> value != null && !value.isBlank()).count();
    return (int) Math.round(filled * 100.0 / 8.0);
  }
}
