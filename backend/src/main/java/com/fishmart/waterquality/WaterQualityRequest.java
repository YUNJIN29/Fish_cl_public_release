package com.fishmart.waterquality;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public class WaterQualityRequest {
  @NotBlank @Size(max = 64) private String pondName;
  @DecimalMin("-5.0") @DecimalMax("50.0") private BigDecimal temperature;
  @DecimalMin("0.0") @DecimalMax("14.0") private BigDecimal ph;
  @DecimalMin("0.0") @DecimalMax("50.0") private BigDecimal dissolvedOxygen;
  @DecimalMin("0.0") @DecimalMax("1000.0") private BigDecimal ammonia;
  @DecimalMin("0.0") @DecimalMax("1000.0") private BigDecimal nitrite;
  @Size(max = 500) private String notes;
  @NotNull private Instant observedAt;

  public boolean hasMeasurement() {
    return temperature != null || ph != null || dissolvedOxygen != null || ammonia != null || nitrite != null;
  }
}
