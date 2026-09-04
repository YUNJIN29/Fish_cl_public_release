package com.fishmart.rag.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RagDatasetDto {
  private String name;
  private String organization;
  private String url;
  private String type;
  private String language;
  private String useCase;
  private String ingestionAdvice;
}
