package com.fishmart.diagnose.dto;

import com.fishmart.diagnose.DiagnoseRecord;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
  private String confidence;
  private List<String> evidence;
  private List<String> sources;
  private List<String> warnings;
  private List<String> nextSteps;
  private boolean legacyFormat;

  private static final ObjectMapper JSON = new ObjectMapper();

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
    dto.confidence = record.getConfidence();
    dto.evidence = readList(record.getEvidenceJson());
    dto.sources = readList(record.getSourcesJson());
    dto.warnings = readList(record.getWarningsJson());
    dto.nextSteps = readList(record.getNextStepsJson());
    dto.legacyFormat = record.getConfidence() == null;
    return dto;
  }

  private static List<String> readList(String value) {
    if (value == null || value.isBlank()) return List.of();
    try { return JSON.readValue(value, new TypeReference<>() {}); }
    catch (Exception ignored) { return List.of(); }
  }
}
