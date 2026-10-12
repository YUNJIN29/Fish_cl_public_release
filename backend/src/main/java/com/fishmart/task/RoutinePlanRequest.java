package com.fishmart.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;
import java.util.Set;

@Data
public class RoutinePlanRequest {
  @NotBlank @Size(max = 64) private String pondName;
  @NotNull private Instant startAt;
  @NotEmpty @Size(max = 4)
  private Set<@Pattern(regexp = "FEEDING|OBSERVATION|WATER_TEST|EQUIPMENT_CHECK") String> presets;
}
