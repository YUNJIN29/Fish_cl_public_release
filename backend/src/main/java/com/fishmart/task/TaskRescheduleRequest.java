package com.fishmart.task;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaskRescheduleRequest {
  @NotNull @Min(1) @Max(365)
  private Integer days;
}
