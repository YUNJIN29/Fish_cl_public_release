package com.fishmart.personal.dto;

import com.fishmart.waterquality.WaterQualityDto;
import com.fishmart.waterquality.WaterQualityRecord;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class WaterQualityAttentionDto {
  private WaterQualityDto record;
  private List<String> deviations;
  private long ageDays;
  private boolean stale;

  public static WaterQualityAttentionDto from(WaterQualityRecord record, List<String> deviations, long ageDays, boolean stale) {
    return new WaterQualityAttentionDto(WaterQualityDto.from(record), deviations, ageDays, stale);
  }
}
