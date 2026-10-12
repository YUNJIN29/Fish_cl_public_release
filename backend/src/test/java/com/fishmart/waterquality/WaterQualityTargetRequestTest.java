package com.fishmart.waterquality;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class WaterQualityTargetRequestTest {
  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test void acceptsUserDefinedPartialTarget() {
    WaterQualityTargetRequest request = base(); request.setPhMin(new BigDecimal("6.8")); request.setPhMax(new BigDecimal("7.8"));
    assertThat(validator.validate(request)).isEmpty(); assertThat(request.hasTarget()).isTrue(); assertThat(request.hasValidRanges()).isTrue();
  }

  @Test void rejectsReversedRange() {
    WaterQualityTargetRequest request = base(); request.setTemperatureMin(new BigDecimal("30")); request.setTemperatureMax(new BigDecimal("20"));
    assertThat(request.hasValidRanges()).isFalse();
  }

  @Test void rejectsOutOfBoundsTarget() {
    WaterQualityTargetRequest request = base(); request.setPhMax(new BigDecimal("20"));
    assertThat(validator.validate(request)).hasSize(1);
  }

  private WaterQualityTargetRequest base() { WaterQualityTargetRequest request = new WaterQualityTargetRequest(); request.setPondName("一号池"); return request; }
}
