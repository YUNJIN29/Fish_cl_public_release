package com.fishmart.diagnose.dto;

import com.fishmart.diagnose.DiagnosisFollowUp;
import lombok.Data;

import java.time.Instant;

@Data
public class DiagnosisFollowUpDto {
  private Long id;
  private String conditionStatus;
  private String appetiteStatus;
  private Integer mortalityCount;
  private String waterQuality;
  private String notes;
  private Instant observedAt;
  private Instant createdAt;

  public static DiagnosisFollowUpDto from(DiagnosisFollowUp item) {
    DiagnosisFollowUpDto dto = new DiagnosisFollowUpDto();
    dto.id = item.getId();
    dto.conditionStatus = item.getConditionStatus();
    dto.appetiteStatus = item.getAppetiteStatus();
    dto.mortalityCount = item.getMortalityCount();
    dto.waterQuality = item.getWaterQuality();
    dto.notes = item.getNotes();
    dto.observedAt = item.getObservedAt();
    dto.createdAt = item.getCreatedAt();
    return dto;
  }
}
