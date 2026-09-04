package com.fishmart.disease.dto;

import com.fishmart.disease.Disease;
import lombok.Data;

import java.time.Instant;

@Data
public class DiseaseDetailDto {
  private Long id;
  private String name;
  private String diseaseType;
  private String description;
  private String prevalentRegions;
  private String affectedSpecies;
  private String causes;
  private String symptomsSummary;
  private String treatment;
  private String prevention;
  private String prevalentTemp;
  private String imageUrl;
  private Instant createdAt;

  public static DiseaseDetailDto from(Disease d) {
    DiseaseDetailDto dto = new DiseaseDetailDto();
    dto.id = d.getId();
    dto.name = d.getName();
    dto.diseaseType = d.getDiseaseType();
    dto.description = d.getDescription();
    dto.prevalentRegions = d.getPrevalentRegions();
    dto.affectedSpecies = d.getAffectedSpecies();
    dto.causes = d.getCauses();
    dto.symptomsSummary = d.getSymptomsSummary();
    dto.treatment = d.getTreatment();
    dto.prevention = d.getPrevention();
    dto.prevalentTemp = d.getPrevalentTemp();
    dto.imageUrl = d.getImageUrl();
    dto.createdAt = d.getCreatedAt();
    return dto;
  }
}
