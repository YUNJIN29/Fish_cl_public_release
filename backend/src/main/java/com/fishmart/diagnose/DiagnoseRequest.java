package com.fishmart.diagnose;

import lombok.Data;

@Data
public class DiagnoseRequest {
  private String description;
  private String imageBase64;
  private String imageMimeType;
  private String species;
  private String temperature;
  private String waterQuality;
  private String duration;
}
