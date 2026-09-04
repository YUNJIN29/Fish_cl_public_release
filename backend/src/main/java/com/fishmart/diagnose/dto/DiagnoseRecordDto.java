package com.fishmart.diagnose.dto;

import com.fishmart.diagnose.DiagnoseRecord;
import lombok.Data;

import java.time.Instant;

@Data
public class DiagnoseRecordDto {
  private Long id;
  private String description;
  private String result;
  private String species;
  private String temperature;
  private String waterQuality;
  private String duration;
  private String imageUrl;
  private Instant createdAt;

  public static DiagnoseRecordDto from(DiagnoseRecord record) {
    DiagnoseRecordDto dto = new DiagnoseRecordDto();
    dto.id = record.getId();
    dto.description = record.getDescription();
    dto.result = record.getResult();
    dto.species = record.getSpecies();
    dto.temperature = record.getTemperature();
    dto.waterQuality = record.getWaterQuality();
    dto.duration = record.getDuration();
    dto.imageUrl = record.getImageUrl();
    dto.createdAt = record.getCreatedAt();
    return dto;
  }
}
