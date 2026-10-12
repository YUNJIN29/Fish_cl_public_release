package com.fishmart.diagnose.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;

@Data
public class DiagnosisFollowUpRequest {
  @NotBlank
  @Pattern(regexp = "IMPROVING|STABLE|WORSENING|RECOVERED", message = "病情状态无效")
  private String conditionStatus;

  @NotBlank
  @Pattern(regexp = "GOOD|REDUCED|NONE", message = "摄食状态无效")
  private String appetiteStatus;

  @NotNull
  @Min(value = 0, message = "死亡数量不能小于 0")
  @Max(value = 100000, message = "死亡数量超出范围")
  private Integer mortalityCount;

  @Size(max = 255)
  private String waterQuality;

  @Size(max = 1000)
  private String notes;

  @NotNull
  private Instant observedAt;
}
