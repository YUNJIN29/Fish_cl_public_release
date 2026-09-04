package com.fishmart.disease.dto;

import com.fishmart.disease.Disease;
import lombok.Data;

@Data
public class DiseaseListItemDto {
  private Long id;
  private String name;
  private String diseaseType;
  private String affectedSpecies;
  private String prevalentRegions;
  private String symptomsSummary;
  private String prevalentTemp;
  private String imageUrl;

  public static DiseaseListItemDto from(Disease d) {
    DiseaseListItemDto dto = new DiseaseListItemDto();
    dto.id = d.getId();
    dto.name = d.getName();
    dto.diseaseType = d.getDiseaseType();
    dto.affectedSpecies = d.getAffectedSpecies();
    dto.prevalentRegions = d.getPrevalentRegions();
    dto.symptomsSummary = d.getSymptomsSummary() == null ? null
        : (d.getSymptomsSummary().length() > 120
            ? d.getSymptomsSummary().substring(0, 120) + "…"
            : d.getSymptomsSummary());
    dto.prevalentTemp = d.getPrevalentTemp();
    dto.imageUrl = d.getImageUrl();
    return dto;
  }
}
