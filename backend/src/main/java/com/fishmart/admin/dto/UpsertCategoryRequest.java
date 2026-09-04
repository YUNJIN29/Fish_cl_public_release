package com.fishmart.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpsertCategoryRequest {
  @NotBlank
  @Size(max = 64)
  private String name;

  @NotNull
  private Integer sortOrder;
}

