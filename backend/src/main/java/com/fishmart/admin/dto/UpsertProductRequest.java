package com.fishmart.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpsertProductRequest {
  @NotNull
  private Long categoryId;

  @NotBlank
  @Size(max = 128)
  private String name;

  private String description;

  @NotNull
  @Min(0)
  private Integer priceCents;

  @NotNull
  @Min(0)
  private Integer stock;

  private String coverImageUrl;

  private String symptomTags;

  private String usageAdvice;

  // 可选：同时设置多图
  private List<String> images;
}

