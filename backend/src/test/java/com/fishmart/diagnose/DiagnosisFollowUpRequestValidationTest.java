package com.fishmart.diagnose;

import com.fishmart.diagnose.dto.DiagnosisFollowUpRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class DiagnosisFollowUpRequestValidationTest {
  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void acceptsAUsefulObservation() {
    DiagnosisFollowUpRequest request = request("IMPROVING", "REDUCED", 0);
    request.setNotes("白点减少，游动开始恢复");
    assertThat(validator.validate(request)).isEmpty();
  }

  @Test
  void acceptsRecoveredAsAClosingObservation() {
    DiagnosisFollowUpRequest request = request("RECOVERED", "GOOD", 0);
    request.setNotes("症状消失，摄食恢复，结束观察");
    assertThat(validator.validate(request)).isEmpty();
  }

  @Test
  void rejectsUnknownStatesAndNegativeMortality() {
    DiagnosisFollowUpRequest request = request("UNKNOWN", "MAYBE", -1);
    assertThat(validator.validate(request)).hasSize(3);
  }

  private DiagnosisFollowUpRequest request(String condition, String appetite, int mortality) {
    DiagnosisFollowUpRequest request = new DiagnosisFollowUpRequest();
    request.setConditionStatus(condition);
    request.setAppetiteStatus(appetite);
    request.setMortalityCount(mortality);
    request.setObservedAt(Instant.now());
    return request;
  }
}
