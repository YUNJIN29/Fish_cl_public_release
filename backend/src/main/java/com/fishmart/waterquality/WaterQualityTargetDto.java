package com.fishmart.waterquality;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class WaterQualityTargetDto {
  private Long id; private String pondName; private BigDecimal temperatureMin; private BigDecimal temperatureMax;
  private BigDecimal phMin; private BigDecimal phMax; private BigDecimal dissolvedOxygenMin;
  private BigDecimal ammoniaMax; private BigDecimal nitriteMax; private Instant updatedAt;

  public static WaterQualityTargetDto from(WaterQualityTarget item) {
    WaterQualityTargetDto dto = new WaterQualityTargetDto(); dto.id=item.getId(); dto.pondName=item.getPondName();
    dto.temperatureMin=item.getTemperatureMin(); dto.temperatureMax=item.getTemperatureMax(); dto.phMin=item.getPhMin(); dto.phMax=item.getPhMax();
    dto.dissolvedOxygenMin=item.getDissolvedOxygenMin(); dto.ammoniaMax=item.getAmmoniaMax(); dto.nitriteMax=item.getNitriteMax(); dto.updatedAt=item.getUpdatedAt(); return dto;
  }
}
