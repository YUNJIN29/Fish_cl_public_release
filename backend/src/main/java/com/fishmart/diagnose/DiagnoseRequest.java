package com.fishmart.diagnose;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DiagnoseRequest {
  @NotBlank(message = "请描述鱼类症状")
  @Size(min = 8, max = 2000, message = "症状描述长度应为 8-2000 个字符")
  private String description;
  @Size(max = 14_000_000, message = "图片不能超过 10MB")
  private String imageBase64;
  @Size(max = 64)
  private String imageMimeType;
  @Size(max = 80)
  private String species;
  @Size(max = 40)
  private String temperature;
  @Size(max = 500)
  private String waterQuality;
  @Size(max = 80)
  private String duration;
}
