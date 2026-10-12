package com.fishmart.diagnose;

import com.fishmart.common.BizException;
import com.fishmart.security.AuthUser;
import com.fishmart.user.RoleName;
import com.fishmart.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiagnosisFollowUpControllerTest {
  @Mock DiagnoseRecordRepository diagnoses;
  @Mock DiagnosisFollowUpRepository followUps;
  @Mock UserRepository users;

  @Test
  void doesNotExposeAnotherUsersDiagnosisFollowUps() {
    DiagnosisFollowUpController controller = new DiagnosisFollowUpController(diagnoses, followUps, users);
    AuthUser currentUser = new AuthUser(7L, "owner", Set.of(RoleName.USER));
    when(diagnoses.findByIdAndUserId(99L, 7L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> controller.list(99L, currentUser))
        .isInstanceOf(BizException.class)
        .hasMessage("诊断记录不存在");
    verify(followUps, never()).findByDiagnoseRecordIdAndUserIdOrderByObservedAtDesc(99L, 7L);
  }

  @Test
  void ownerCanCorrectAnExistingFollowUp() {
    DiagnosisFollowUpController controller = new DiagnosisFollowUpController(diagnoses, followUps, users);
    AuthUser currentUser = new AuthUser(7L, "owner", Set.of(RoleName.USER));
    DiagnoseRecord diagnosis = new DiagnoseRecord(); diagnosis.setId(12L); diagnosis.setCreatedAt(Instant.now().minusSeconds(7200));
    DiagnosisFollowUp item = new DiagnosisFollowUp(); item.setId(13L); item.setDiagnoseRecord(diagnosis);
    when(diagnoses.findByIdAndUserId(12L, 7L)).thenReturn(Optional.of(diagnosis));
    when(followUps.findByIdAndUserId(13L, 7L)).thenReturn(Optional.of(item));
    when(followUps.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    var request = new com.fishmart.diagnose.dto.DiagnosisFollowUpRequest();
    request.setConditionStatus("IMPROVING"); request.setAppetiteStatus("GOOD"); request.setMortalityCount(0);
    request.setWaterQuality(" 溶氧正常 "); request.setNotes(" 活动恢复 "); request.setObservedAt(Instant.now().minusSeconds(60));

    var response = controller.update(12L, 13L, request, currentUser);

    assertThat(response.getData().getConditionStatus()).isEqualTo("IMPROVING");
    assertThat(response.getData().getWaterQuality()).isEqualTo("溶氧正常");
    assertThat(response.getData().getNotes()).isEqualTo("活动恢复");
    verify(followUps).save(item);
  }

  @Test
  void cannotUpdateFollowUpFromAnotherDiagnosis() {
    DiagnosisFollowUpController controller = new DiagnosisFollowUpController(diagnoses, followUps, users);
    AuthUser currentUser = new AuthUser(7L, "owner", Set.of(RoleName.USER));
    DiagnoseRecord diagnosis = new DiagnoseRecord(); diagnosis.setId(12L); diagnosis.setCreatedAt(Instant.now().minusSeconds(7200));
    DiagnoseRecord otherDiagnosis = new DiagnoseRecord(); otherDiagnosis.setId(99L);
    DiagnosisFollowUp item = new DiagnosisFollowUp(); item.setId(13L); item.setDiagnoseRecord(otherDiagnosis);
    when(diagnoses.findByIdAndUserId(12L, 7L)).thenReturn(Optional.of(diagnosis));
    when(followUps.findByIdAndUserId(13L, 7L)).thenReturn(Optional.of(item));
    var request = new com.fishmart.diagnose.dto.DiagnosisFollowUpRequest();
    request.setConditionStatus("STABLE"); request.setAppetiteStatus("REDUCED"); request.setMortalityCount(0); request.setObservedAt(Instant.now());

    assertThatThrownBy(() -> controller.update(12L, 13L, request, currentUser))
        .isInstanceOf(BizException.class).hasMessage("观察记录不存在");
    verify(followUps, never()).save(any());
  }
}
