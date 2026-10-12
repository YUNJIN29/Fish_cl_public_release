package com.fishmart.personal;

import com.fishmart.diagnose.DiagnoseRecord;
import com.fishmart.diagnose.DiagnosisFollowUp;
import com.fishmart.waterquality.WaterQualityRecord;
import com.fishmart.waterquality.WaterQualityTarget;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.List;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class PersonalControllerAttentionTest {
  @Test
  void onlyLatestObservationDeterminesWhetherDiagnosisNeedsAttention() {
    DiagnoseRecord recoveredDiagnosis = diagnosis(1L);
    DiagnoseRecord mortalityDiagnosis = diagnosis(2L);
    List<DiagnosisFollowUp> newestFirst = List.of(
        observation(recoveredDiagnosis, "RECOVERED", 1, "2026-10-12T10:00:00Z"),
        observation(mortalityDiagnosis, "STABLE", 2, "2026-10-12T09:00:00Z"),
        observation(recoveredDiagnosis, "WORSENING", 1, "2026-10-12T08:00:00Z")
    );

    var result = PersonalController.attentionFrom(newestFirst);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getDiagnosis().getId()).isEqualTo(2L);
    assertThat(result.get(0).getLatestObservation().getMortalityCount()).isEqualTo(2);
  }

  @Test
  void onlyLatestWaterReadingPerPondCanTriggerAttention() {
    Instant now = Instant.now();
    WaterQualityRecord latestSafe = waterRecord("一号池", "7.2", now.toString());
    WaterQualityRecord olderUnsafe = waterRecord("一号池", "8.8", now.minus(2, ChronoUnit.HOURS).toString());
    WaterQualityRecord latestUnsafe = waterRecord("二号池", "6.3", now.minus(1, ChronoUnit.HOURS).toString());
    WaterQualityRecord staleWithoutTarget = waterRecord("三号池", "7.2", now.minus(9, ChronoUnit.DAYS).toString());
    WaterQualityTarget first = target("一号池", "6.8", "7.8");
    WaterQualityTarget second = target("二号池", "6.8", "7.8");

    var result = PersonalController.waterAttentionFrom(List.of(latestSafe, latestUnsafe, olderUnsafe, staleWithoutTarget), List.of(first, second));

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getRecord().getPondName()).isEqualTo("二号池");
    assertThat(result.get(0).getDeviations()).containsExactly("pH");
    assertThat(result.get(0).isStale()).isFalse();
    assertThat(result.get(1).getRecord().getPondName()).isEqualTo("三号池");
    assertThat(result.get(1).getDeviations()).isEmpty();
    assertThat(result.get(1).isStale()).isTrue();
    assertThat(result.get(1).getAgeDays()).isGreaterThanOrEqualTo(8);
  }

  private DiagnoseRecord diagnosis(Long id) {
    DiagnoseRecord record = new DiagnoseRecord();
    record.setId(id);
    record.setDescription("测试病例 " + id);
    record.setResult("观察建议");
    record.setCreatedAt(Instant.parse("2026-10-12T07:00:00Z"));
    return record;
  }

  private DiagnosisFollowUp observation(DiagnoseRecord record, String status, int mortality, String observedAt) {
    DiagnosisFollowUp item = new DiagnosisFollowUp();
    item.setId((long) observedAt.hashCode());
    item.setDiagnoseRecord(record);
    item.setConditionStatus(status);
    item.setAppetiteStatus("REDUCED");
    item.setMortalityCount(mortality);
    item.setObservedAt(Instant.parse(observedAt));
    return item;
  }

  private WaterQualityRecord waterRecord(String pond, String ph, String observedAt) {
    WaterQualityRecord record = new WaterQualityRecord(); record.setId((long) observedAt.hashCode()); record.setPondName(pond);
    record.setPh(new BigDecimal(ph)); record.setObservedAt(Instant.parse(observedAt)); return record;
  }

  private WaterQualityTarget target(String pond, String min, String max) {
    WaterQualityTarget target = new WaterQualityTarget(); target.setPondName(pond); target.setPhMin(new BigDecimal(min)); target.setPhMax(new BigDecimal(max)); return target;
  }
}
