package com.fishmart.diagnose;

import com.fishmart.common.ApiResponse;
import com.fishmart.common.BizException;
import com.fishmart.diagnose.dto.DiagnosisFollowUpDto;
import com.fishmart.diagnose.dto.DiagnosisFollowUpRequest;
import com.fishmart.security.AuthUser;
import com.fishmart.user.User;
import com.fishmart.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/diagnose/{diagnosisId}/follow-ups")
public class DiagnosisFollowUpController {
  private final DiagnoseRecordRepository diagnoses;
  private final DiagnosisFollowUpRepository followUps;
  private final UserRepository users;

  public DiagnosisFollowUpController(DiagnoseRecordRepository diagnoses,
                                     DiagnosisFollowUpRepository followUps,
                                     UserRepository users) {
    this.diagnoses = diagnoses;
    this.followUps = followUps;
    this.users = users;
  }

  @GetMapping
  public ApiResponse<List<DiagnosisFollowUpDto>> list(@PathVariable Long diagnosisId,
                                                       @AuthenticationPrincipal AuthUser authUser) {
    ownedDiagnosis(diagnosisId, authUser);
    return ApiResponse.ok(followUps.findByDiagnoseRecordIdAndUserIdOrderByObservedAtDesc(diagnosisId, authUser.getId())
        .stream().map(DiagnosisFollowUpDto::from).toList());
  }

  @PostMapping
  public ApiResponse<DiagnosisFollowUpDto> create(@PathVariable Long diagnosisId,
                                                   @Valid @RequestBody DiagnosisFollowUpRequest request,
                                                   @AuthenticationPrincipal AuthUser authUser) {
    DiagnoseRecord diagnosis = ownedDiagnosis(diagnosisId, authUser);
    if (followUps.countByDiagnoseRecordIdAndUserId(diagnosisId, authUser.getId()) >= 100) {
      throw new BizException("单次诊断最多保存 100 条观察记录");
    }
    validateObservedAt(request.getObservedAt(), diagnosis);
    User user = users.findById(authUser.getId()).orElseThrow(() -> new BizException("用户不存在"));
    DiagnosisFollowUp item = new DiagnosisFollowUp();
    item.setDiagnoseRecord(diagnosis);
    item.setUser(user);
    apply(item, request);
    return ApiResponse.ok("观察记录已保存", DiagnosisFollowUpDto.from(followUps.save(item)));
  }

  @PutMapping("/{followUpId}")
  public ApiResponse<DiagnosisFollowUpDto> update(@PathVariable Long diagnosisId,
                                                   @PathVariable Long followUpId,
                                                   @Valid @RequestBody DiagnosisFollowUpRequest request,
                                                   @AuthenticationPrincipal AuthUser authUser) {
    DiagnoseRecord diagnosis = ownedDiagnosis(diagnosisId, authUser);
    DiagnosisFollowUp item = ownedFollowUp(diagnosisId, followUpId, authUser);
    validateObservedAt(request.getObservedAt(), diagnosis);
    apply(item, request);
    return ApiResponse.ok("观察记录已更新", DiagnosisFollowUpDto.from(followUps.save(item)));
  }

  @DeleteMapping("/{followUpId}")
  public ApiResponse<Void> delete(@PathVariable Long diagnosisId,
                                  @PathVariable Long followUpId,
                                  @AuthenticationPrincipal AuthUser authUser) {
    ownedDiagnosis(diagnosisId, authUser);
    DiagnosisFollowUp item = ownedFollowUp(diagnosisId, followUpId, authUser);
    followUps.delete(item);
    return ApiResponse.ok("观察记录已删除", null);
  }

  private DiagnoseRecord ownedDiagnosis(Long diagnosisId, AuthUser authUser) {
    if (authUser == null) throw new BizException("未登录");
    return diagnoses.findByIdAndUserId(diagnosisId, authUser.getId())
        .orElseThrow(() -> new BizException("诊断记录不存在"));
  }

  private DiagnosisFollowUp ownedFollowUp(Long diagnosisId, Long followUpId, AuthUser authUser) {
    return followUps.findByIdAndUserId(followUpId, authUser.getId())
        .filter(value -> value.getDiagnoseRecord().getId().equals(diagnosisId))
        .orElseThrow(() -> new BizException("观察记录不存在"));
  }

  private void validateObservedAt(Instant observedAt, DiagnoseRecord diagnosis) {
    Instant now = Instant.now();
    if (observedAt.isAfter(now.plus(5, ChronoUnit.MINUTES)) ||
        observedAt.isBefore(diagnosis.getCreatedAt().minus(5, ChronoUnit.MINUTES))) {
      throw new BizException("观察时间不能早于诊断时间或晚于当前时间");
    }
  }

  private void apply(DiagnosisFollowUp item, DiagnosisFollowUpRequest request) {
    item.setConditionStatus(request.getConditionStatus());
    item.setAppetiteStatus(request.getAppetiteStatus());
    item.setMortalityCount(request.getMortalityCount());
    item.setWaterQuality(trimToNull(request.getWaterQuality()));
    item.setNotes(trimToNull(request.getNotes()));
    item.setObservedAt(request.getObservedAt());
  }

  private String trimToNull(String value) {
    if (value == null || value.isBlank()) return null;
    return value.trim();
  }
}
