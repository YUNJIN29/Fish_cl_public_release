package com.fishmart.waterquality;

import com.fishmart.common.BizException;
import com.fishmart.security.AuthUser;
import com.fishmart.user.RoleName;
import com.fishmart.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;
import java.util.Set;
import java.time.Instant;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WaterQualityControllerTest {
  @Mock WaterQualityRepository records;
  @Mock WaterQualityTargetRepository targets;
  @Mock UserRepository users;

  @Test
  void cannotDeleteAnotherUsersRecord() {
    WaterQualityController controller = new WaterQualityController(records, targets, users);
    AuthUser currentUser = new AuthUser(7L, "owner", Set.of(RoleName.USER));
    when(records.findByIdAndUserId(99L, 7L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> controller.delete(99L, currentUser))
        .isInstanceOf(BizException.class)
        .hasMessage("水质记录不存在");
    verify(records, never()).delete(any());
  }

  @Test
  void cannotReadAnotherUsersRecordById() {
    WaterQualityController controller = new WaterQualityController(records, targets, users);
    AuthUser currentUser = new AuthUser(7L, "owner", Set.of(RoleName.USER));
    when(records.findByIdAndUserId(99L, 7L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> controller.detail(99L, currentUser))
        .isInstanceOf(BizException.class)
        .hasMessage("水质记录不存在");
  }

  @Test
  void readsOwnedWaterQualityRecordById() {
    WaterQualityController controller = new WaterQualityController(records, targets, users);
    AuthUser currentUser = new AuthUser(7L, "owner", Set.of(RoleName.USER));
    WaterQualityRecord item = new WaterQualityRecord(); item.setId(18L); item.setPondName("育苗池"); item.setObservedAt(Instant.parse("2026-10-12T01:30:00Z"));
    when(records.findByIdAndUserId(18L, 7L)).thenReturn(Optional.of(item));

    assertThat(controller.detail(18L, currentUser).getData().getPondName()).isEqualTo("育苗池");
  }

  @Test
  void rejectsTrendWithoutPondName() {
    WaterQualityController controller = new WaterQualityController(records, targets, users);
    AuthUser currentUser = new AuthUser(7L, "owner", Set.of(RoleName.USER));

    assertThatThrownBy(() -> controller.trend(currentUser, "  ", 30))
        .isInstanceOf(BizException.class)
        .hasMessage("请选择有效水体");
    verify(records, never()).findByUserIdAndPondNameAndObservedAtAfterOrderByObservedAtAsc(any(), any(), any(), any());
  }

  @Test
  void cannotDeleteAnotherUsersTarget() {
    WaterQualityController controller = new WaterQualityController(records, targets, users);
    AuthUser currentUser = new AuthUser(7L, "owner", Set.of(RoleName.USER));
    when(targets.findByIdAndUserId(55L, 7L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> controller.deleteTarget(55L, currentUser))
        .isInstanceOf(BizException.class)
        .hasMessage("参考范围不存在");
    verify(targets, never()).delete(any());
  }

  @Test
  void cannotUpdateAnotherUsersRecord() {
    WaterQualityController controller = new WaterQualityController(records, targets, users);
    AuthUser currentUser = new AuthUser(7L, "owner", Set.of(RoleName.USER));
    WaterQualityRequest request = new WaterQualityRequest(); request.setPondName("一号池"); request.setPh(new BigDecimal("7.2")); request.setObservedAt(Instant.now());
    when(records.findByIdAndUserId(88L, 7L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> controller.update(88L, currentUser, request))
        .isInstanceOf(BizException.class)
        .hasMessage("水质记录不存在");
    verify(records, never()).save(any());
  }

  @Test
  void exportsOnlyCurrentUsersCompleteHistoryAsSafeUtf8Csv() {
    WaterQualityController controller = new WaterQualityController(records, targets, users);
    AuthUser currentUser = new AuthUser(7L, "owner", Set.of(RoleName.USER));
    WaterQualityRecord item = new WaterQualityRecord();
    item.setPondName("育苗池");
    item.setObservedAt(Instant.parse("2026-10-12T01:30:00Z"));
    item.setPh(new BigDecimal("7.20"));
    item.setNotes("=HYPERLINK(\"https://example.com\")\n换水前");
    when(records.findByUserIdOrderByObservedAtDesc(7L)).thenReturn(List.of(item));

    var response = controller.export(currentUser, null);
    String csv = new String(response.getBody(), StandardCharsets.UTF_8);

    assertThat(response.getHeaders().getContentDisposition().isAttachment()).isTrue();
    assertThat(csv).startsWith("\uFEFF水体名称,测量时间");
    assertThat(csv).contains("\"育苗池\"", "\"7.20\"", "\"'=HYPERLINK");
    assertThat(csv).doesNotContain("\n换水前");
    verify(records).findByUserIdOrderByObservedAtDesc(7L);
  }

  @Test
  void rejectsAnonymousWaterQualityExport() {
    WaterQualityController controller = new WaterQualityController(records, targets, users);
    assertThatThrownBy(() -> controller.export(null, null))
        .isInstanceOf(BizException.class)
        .hasMessage("未登录");
    verify(records, never()).findByUserIdOrderByObservedAtDesc(any());
  }

  @Test
  void exportsOnlySelectedPondForCurrentUser() {
    WaterQualityController controller = new WaterQualityController(records, targets, users);
    AuthUser currentUser = new AuthUser(7L, "owner", Set.of(RoleName.USER));
    WaterQualityRecord item = new WaterQualityRecord();
    item.setPondName("一号池");
    item.setObservedAt(Instant.parse("2026-10-12T01:30:00Z"));
    item.setPh(new BigDecimal("7.50"));
    when(records.findByUserIdAndPondNameOrderByObservedAtDesc(7L, "一号池")).thenReturn(List.of(item));

    var response = controller.export(currentUser, " 一号池 ");
    String csv = new String(response.getBody(), StandardCharsets.UTF_8);

    assertThat(csv).contains("\"一号池\"", "\"7.50\"");
    verify(records).findByUserIdAndPondNameOrderByObservedAtDesc(7L, "一号池");
    verify(records, never()).findByUserIdOrderByObservedAtDesc(any());
  }

  @Test
  void rejectsOverlongPondNameForExport() {
    WaterQualityController controller = new WaterQualityController(records, targets, users);
    AuthUser currentUser = new AuthUser(7L, "owner", Set.of(RoleName.USER));

    assertThatThrownBy(() -> controller.export(currentUser, "池".repeat(65)))
        .isInstanceOf(BizException.class)
        .hasMessage("请选择有效水体");
    verify(records, never()).findByUserIdOrderByObservedAtDesc(any());
    verify(records, never()).findByUserIdAndPondNameOrderByObservedAtDesc(any(), any());
  }

  @Test
  void overviewReturnsLatestReadingPerPondAndPersonalDeviations() {
    WaterQualityController controller = new WaterQualityController(records, targets, users);
    AuthUser currentUser = new AuthUser(7L, "owner", Set.of(RoleName.USER));
    WaterQualityRecord newest = new WaterQualityRecord(); newest.setPondName("一号池"); newest.setObservedAt(Instant.parse("2026-10-12T02:00:00Z")); newest.setPh(new BigDecimal("9.1")); newest.setDissolvedOxygen(new BigDecimal("3.0"));
    WaterQualityRecord older = new WaterQualityRecord(); older.setPondName("一号池"); older.setObservedAt(Instant.parse("2026-10-11T02:00:00Z")); older.setPh(new BigDecimal("7.2"));
    WaterQualityRecord secondPond = new WaterQualityRecord(); secondPond.setPondName("二号池"); secondPond.setObservedAt(Instant.now().minus(9, java.time.temporal.ChronoUnit.DAYS)); secondPond.setPh(new BigDecimal("7.5"));
    WaterQualityTarget target = new WaterQualityTarget(); target.setPondName("一号池"); target.setPhMax(new BigDecimal("8.5")); target.setDissolvedOxygenMin(new BigDecimal("5.0"));
    when(records.findByUserIdOrderByObservedAtDesc(7L)).thenReturn(List.of(newest, older, secondPond));
    when(targets.findByUserIdOrderByPondName(7L)).thenReturn(List.of(target));

    var result = controller.overview(currentUser).getData();

    assertThat(result).hasSize(2);
    assertThat(result.get(0).pondName()).isEqualTo("一号池");
    assertThat(result.get(0).latest().getObservedAt()).isEqualTo(newest.getObservedAt());
    assertThat(result.get(0).hasTarget()).isTrue();
    assertThat(result.get(0).deviations()).containsExactly("pH", "溶氧");
    assertThat(result.get(1).hasTarget()).isFalse();
    assertThat(result.get(1).stale()).isTrue();
    assertThat(result.get(1).ageDays()).isGreaterThanOrEqualTo(8);
  }

  @Test
  void rejectsAnonymousWaterQualityOverview() {
    WaterQualityController controller = new WaterQualityController(records, targets, users);
    assertThatThrownBy(() -> controller.overview(null)).isInstanceOf(BizException.class).hasMessage("未登录");
    verify(records, never()).findByUserIdOrderByObservedAtDesc(any());
  }
}
