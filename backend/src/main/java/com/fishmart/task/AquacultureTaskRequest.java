package com.fishmart.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;

@Data
public class AquacultureTaskRequest {
  @NotBlank @Size(max = 100) private String title;
  @NotBlank @Pattern(regexp = "FEEDING|WATER_TEST|WATER_CHANGE|MEDICATION|OBSERVATION|OTHER") private String category;
  @Size(max = 64) private String pondName;
  @NotNull private Instant dueAt;
  @Size(max = 500) private String notes;
  @Min(1) @Max(365) private Integer repeatDays;
}
