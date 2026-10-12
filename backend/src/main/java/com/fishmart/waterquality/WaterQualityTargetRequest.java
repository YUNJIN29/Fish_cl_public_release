package com.fishmart.waterquality;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WaterQualityTargetRequest {
  @NotBlank @Size(max = 64) private String pondName;
  @DecimalMin("-5") @DecimalMax("50") private BigDecimal temperatureMin;
  @DecimalMin("-5") @DecimalMax("50") private BigDecimal temperatureMax;
  @DecimalMin("0") @DecimalMax("14") private BigDecimal phMin;
  @DecimalMin("0") @DecimalMax("14") private BigDecimal phMax;
  @DecimalMin("0") @DecimalMax("50") private BigDecimal dissolvedOxygenMin;
  @DecimalMin("0") @DecimalMax("1000") private BigDecimal ammoniaMax;
  @DecimalMin("0") @DecimalMax("1000") private BigDecimal nitriteMax;

  public boolean hasTarget() {
    return temperatureMin != null || temperatureMax != null || phMin != null || phMax != null
        || dissolvedOxygenMin != null || ammoniaMax != null || nitriteMax != null;
  }

  public boolean hasValidRanges() {
    return (temperatureMin == null || temperatureMax == null || temperatureMin.compareTo(temperatureMax) <= 0)
        && (phMin == null || phMax == null || phMin.compareTo(phMax) <= 0);
  }
}
