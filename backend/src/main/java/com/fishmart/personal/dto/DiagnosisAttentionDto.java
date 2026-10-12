package com.fishmart.personal.dto;

import com.fishmart.diagnose.DiagnosisFollowUp;
import com.fishmart.diagnose.dto.DiagnoseRecordDto;
import com.fishmart.diagnose.dto.DiagnosisFollowUpDto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DiagnosisAttentionDto {
  private DiagnoseRecordDto diagnosis;
  private DiagnosisFollowUpDto latestObservation;

  public static DiagnosisAttentionDto from(DiagnosisFollowUp followUp) {
    return new DiagnosisAttentionDto(
        DiagnoseRecordDto.from(followUp.getDiagnoseRecord()),
        DiagnosisFollowUpDto.from(followUp)
    );
  }
}
