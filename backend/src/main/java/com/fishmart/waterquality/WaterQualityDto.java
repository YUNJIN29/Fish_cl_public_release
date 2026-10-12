package com.fishmart.waterquality;

import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public class WaterQualityDto {
  private Long id; private String pondName; private BigDecimal temperature; private BigDecimal ph;
  private BigDecimal dissolvedOxygen; private BigDecimal ammonia; private BigDecimal nitrite;
  private String notes; private Instant observedAt; private Instant createdAt;
  public static WaterQualityDto from(WaterQualityRecord item) {
    WaterQualityDto dto = new WaterQualityDto(); dto.id=item.getId(); dto.pondName=item.getPondName();
    dto.temperature=item.getTemperature(); dto.ph=item.getPh(); dto.dissolvedOxygen=item.getDissolvedOxygen();
    dto.ammonia=item.getAmmonia(); dto.nitrite=item.getNitrite(); dto.notes=item.getNotes();
    dto.observedAt=item.getObservedAt(); dto.createdAt=item.getCreatedAt(); return dto;
  }
}
