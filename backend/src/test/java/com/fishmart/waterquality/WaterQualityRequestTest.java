package com.fishmart.waterquality;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import static org.assertj.core.api.Assertions.assertThat;

class WaterQualityRequestTest {
  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
  @Test void acceptsPartialMeasurements() { WaterQualityRequest r=base(); r.setPh(new BigDecimal("7.40")); assertThat(validator.validate(r)).isEmpty(); assertThat(r.hasMeasurement()).isTrue(); }
  @Test void rejectsImpossiblePhysicalRanges() { WaterQualityRequest r=base(); r.setPh(new BigDecimal("15")); r.setTemperature(new BigDecimal("80")); assertThat(validator.validate(r)).hasSize(2); }
  @Test void detectsMissingMeasurements() { assertThat(base().hasMeasurement()).isFalse(); }
  private WaterQualityRequest base() { WaterQualityRequest r=new WaterQualityRequest(); r.setPondName("一号池"); r.setObservedAt(Instant.now()); return r; }
}
