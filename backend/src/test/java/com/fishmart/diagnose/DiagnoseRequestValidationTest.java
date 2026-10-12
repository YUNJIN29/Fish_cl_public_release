package com.fishmart.diagnose;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DiagnoseRequestValidationTest {
  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void rejectsBlankOrTooShortDescription() {
    DiagnoseRequest request = new DiagnoseRequest();
    request.setDescription("白点");
    assertThat(validator.validate(request)).isNotEmpty();
  }

  @Test
  void acceptsUsefulDescription() {
    DiagnoseRequest request = new DiagnoseRequest();
    request.setDescription("草鱼体表出现白色小点并且食欲明显下降");
    request.setSpecies("草鱼");
    assertThat(validator.validate(request)).isEmpty();
  }
}
