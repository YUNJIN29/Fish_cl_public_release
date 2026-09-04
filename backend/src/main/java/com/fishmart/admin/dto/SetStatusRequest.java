package com.fishmart.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SetStatusRequest {
  @NotNull
  private String status;
}

