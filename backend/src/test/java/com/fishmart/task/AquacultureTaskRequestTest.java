package com.fishmart.task;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AquacultureTaskRequestTest {
  private final Validator validator= Validation.buildDefaultValidatorFactory().getValidator();
  @Test void acceptsSupportedCategory(){AquacultureTaskRequest request=base();request.setCategory("WATER_TEST");assertThat(validator.validate(request)).isEmpty();}
  @Test void rejectsUnsupportedCategory(){AquacultureTaskRequest request=base();request.setCategory("UNKNOWN");assertThat(validator.validate(request)).hasSize(1);}
  @Test void requiresDueTime(){AquacultureTaskRequest request=base();request.setDueAt(null);assertThat(validator.validate(request)).hasSize(1);}
  @Test void acceptsRepeatIntervalWithinOneYear(){AquacultureTaskRequest request=base();request.setRepeatDays(7);assertThat(validator.validate(request)).isEmpty();}
  @Test void rejectsRepeatIntervalOutsideRange(){AquacultureTaskRequest request=base();request.setRepeatDays(366);assertThat(validator.validate(request)).hasSize(1);}
  @Test void validatesRescheduleRange(){TaskRescheduleRequest request=new TaskRescheduleRequest();request.setDays(0);assertThat(validator.validate(request)).hasSize(1);request.setDays(365);assertThat(validator.validate(request)).isEmpty();}
  private AquacultureTaskRequest base(){AquacultureTaskRequest request=new AquacultureTaskRequest();request.setTitle("复测水质");request.setCategory("OTHER");request.setDueAt(Instant.now());return request;}
}
