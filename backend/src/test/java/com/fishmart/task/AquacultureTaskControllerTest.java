package com.fishmart.task;

import com.fishmart.common.BizException;
import com.fishmart.security.AuthUser;
import com.fishmart.user.RoleName;
import com.fishmart.user.UserRepository;
import com.fishmart.user.User;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;
import java.util.Set;
import java.time.Instant;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AquacultureTaskControllerTest {
  @Mock AquacultureTaskRepository tasks;
  @Mock UserRepository users;

  @Test void cannotCompleteAnotherUsersTask() {
    AquacultureTaskController controller=new AquacultureTaskController(tasks,users);
    AuthUser current=new AuthUser(7L,"owner", Set.of(RoleName.USER));
    TaskCompletionRequest request=new TaskCompletionRequest();request.setCompleted(true);
    when(tasks.findByIdAndUserId(88L,7L)).thenReturn(Optional.empty());
    assertThatThrownBy(()->controller.completion(88L,current,request)).isInstanceOf(BizException.class).hasMessage("待办不存在");
    verify(tasks,never()).save(any());
  }

  @Test void overdueListScopeUsesCurrentUserAndServerTime() {
    AquacultureTaskController controller=new AquacultureTaskController(tasks,users);
    AuthUser current=new AuthUser(7L,"owner",Set.of(RoleName.USER));
    when(tasks.findByUserIdAndCompletedAtIsNullAndDueAtBeforeOrderByDueAtAsc(eq(7L),any(Instant.class),any(PageRequest.class)))
      .thenReturn(new PageImpl<>(java.util.List.of()));

    var response=controller.list(current,"OPEN","OVERDUE","","","",0,20);

    assertThat(response.getData().getTotalElements()).isZero();
    verify(tasks).findByUserIdAndCompletedAtIsNullAndDueAtBeforeOrderByDueAtAsc(eq(7L),any(Instant.class),any(PageRequest.class));
  }

  @Test void overdueTaskReschedulesFromCurrentTime() {
    AquacultureTaskController controller=new AquacultureTaskController(tasks,users);
    AuthUser current=new AuthUser(7L,"owner",Set.of(RoleName.USER));
    AquacultureTask item=new AquacultureTask();item.setId(15L);item.setDueAt(Instant.parse("2025-01-01T00:00:00Z"));
    when(tasks.findByIdAndUserId(15L,7L)).thenReturn(Optional.of(item));
    when(tasks.save(item)).thenReturn(item);
    TaskRescheduleRequest request=new TaskRescheduleRequest();request.setDays(1);
    Instant before=Instant.now().plus(1,java.time.temporal.ChronoUnit.DAYS);

    var response=controller.reschedule(15L,current,request);

    assertThat(response.getData().getDueAt()).isBetween(before,Instant.now().plus(1,java.time.temporal.ChronoUnit.DAYS));
    assertThat(response.getMessage()).contains("1 天");
    verify(tasks).findByIdAndUserId(15L,7L);
    verify(tasks).save(item);
  }

  @Test void calendarExportContainsOnlyCurrentUsersOpenTasksAndEscapesText() {
    AquacultureTaskController controller=new AquacultureTaskController(tasks,users);
    AuthUser current=new AuthUser(7L,"owner",Set.of(RoleName.USER));
    AquacultureTask item=new AquacultureTask();item.setId(15L);item.setTitle("复测水质,记录");item.setPondName("一号池;北区");item.setNotes("第一行\n第二行");item.setDueAt(Instant.parse("2026-10-13T08:30:00Z"));
    when(tasks.findByUserIdAndCompletedAtIsNullOrderByDueAtAsc(7L)).thenReturn(java.util.List.of(item));

    var response=controller.exportCalendar(current);
    String body=new String(response.getBody(),StandardCharsets.UTF_8);

    assertThat(response.getHeaders().getFirst("Content-Disposition")).contains("aquaculture-tasks.ics");
    assertThat(body).contains("BEGIN:VCALENDAR","UID:task-15@example.com","DTSTART:20261013T083000Z","SUMMARY:复测水质\\,记录","LOCATION:一号池\\;北区","DESCRIPTION:第一行\\n第二行","END:VCALENDAR");
    verify(tasks).findByUserIdAndCompletedAtIsNullOrderByDueAtAsc(7L);
  }

  @Test void completedTaskCannotBeRescheduled() {
    AquacultureTaskController controller=new AquacultureTaskController(tasks,users);
    AuthUser current=new AuthUser(7L,"owner",Set.of(RoleName.USER));
    AquacultureTask item=new AquacultureTask();item.setId(15L);item.setDueAt(Instant.now());item.setCompletedAt(Instant.now());
    when(tasks.findByIdAndUserId(15L,7L)).thenReturn(Optional.of(item));
    TaskRescheduleRequest request=new TaskRescheduleRequest();request.setDays(7);

    assertThatThrownBy(()->controller.reschedule(15L,current,request)).isInstanceOf(BizException.class).hasMessage("已完成待办不能顺延");
    verify(tasks,never()).save(item);
  }

  @Test void summaryOnlyCountsCurrentUsersOpenTasks() {
    AquacultureTaskController controller=new AquacultureTaskController(tasks,users);
    AuthUser current=new AuthUser(7L,"owner",Set.of(RoleName.USER));
    when(tasks.countByUserIdAndCompletedAtIsNull(7L)).thenReturn(9L);
    when(tasks.countByUserIdAndCompletedAtIsNullAndDueAtBefore(eq(7L),any(Instant.class))).thenReturn(2L);
    when(tasks.countByUserIdAndCompletedAtIsNullAndDueAtGreaterThanEqualAndDueAtBefore(eq(7L),any(Instant.class),any(Instant.class))).thenReturn(3L);
    when(tasks.countByUserIdAndCompletedAtIsNullAndRepeatIntervalDaysIsNotNull(7L)).thenReturn(4L);

    var response=controller.summary(current);

    assertThat(response.getData()).isEqualTo(new TaskSummaryDto(9,2,3,4));
    verify(tasks).countByUserIdAndCompletedAtIsNull(7L);
    verify(tasks).countByUserIdAndCompletedAtIsNullAndDueAtBefore(eq(7L),any(Instant.class));
    verify(tasks).countByUserIdAndCompletedAtIsNullAndDueAtGreaterThanEqualAndDueAtBefore(eq(7L),any(Instant.class),any(Instant.class));
    verify(tasks).countByUserIdAndCompletedAtIsNullAndRepeatIntervalDaysIsNotNull(7L);
  }

  @Test void anonymousSummaryIsRejectedBeforeRepositoryAccess() {
    AquacultureTaskController controller=new AquacultureTaskController(tasks,users);
    assertThatThrownBy(()->controller.summary(null)).isInstanceOf(BizException.class).hasMessage("未登录");
    verify(tasks,never()).countByUserIdAndCompletedAtIsNull(any());
  }

  @Test void filteredListSearchesOnlyCurrentUsersTasks() {
    AquacultureTaskController controller=new AquacultureTaskController(tasks,users);
    AuthUser current=new AuthUser(7L,"owner",Set.of(RoleName.USER));
    when(tasks.search(eq(7L),eq("OPEN"),eq("ALL"),eq("WATER_TEST"),eq("一号池"),eq("复测"),any(Instant.class),any(Instant.class),any(PageRequest.class)))
      .thenReturn(new PageImpl<>(java.util.List.of()));

    var response=controller.list(current,"OPEN","ALL","复测","一号池","water_test",0,20);

    assertThat(response.getData().getTotalElements()).isZero();
    verify(tasks).search(eq(7L),eq("OPEN"),eq("ALL"),eq("WATER_TEST"),eq("一号池"),eq("复测"),any(Instant.class),any(Instant.class),any(PageRequest.class));
  }

  @Test void pondOptionsAreScopedToCurrentUser() {
    AquacultureTaskController controller=new AquacultureTaskController(tasks,users);
    AuthUser current=new AuthUser(7L,"owner",Set.of(RoleName.USER));
    when(tasks.findDistinctPondNames(7L)).thenReturn(java.util.List.of("一号池","育苗池"));

    var response=controller.pondNames(current);

    assertThat(response.getData()).containsExactly("一号池","育苗池");
    verify(tasks).findDistinctPondNames(7L);
  }

  @Test void completingRecurringTaskCreatesExactlyOneNextOccurrence() {
    AquacultureTaskController controller=new AquacultureTaskController(tasks,users);
    AuthUser current=new AuthUser(7L,"owner", Set.of(RoleName.USER));
    User owner=new User(); owner.setId(7L);
    AquacultureTask item=new AquacultureTask(); item.setId(12L); item.setUser(owner); item.setTitle("检测水质"); item.setCategory("WATER_TEST");
    item.setPondName("一号池"); item.setDueAt(Instant.parse("2026-10-12T08:00:00Z")); item.setRepeatIntervalDays(7);
    when(tasks.findByIdAndUserId(12L,7L)).thenReturn(Optional.of(item));
    when(tasks.save(any())).thenAnswer(invocation->invocation.getArgument(0));
    TaskCompletionRequest request=new TaskCompletionRequest(); request.setCompleted(true);

    var response=controller.completion(12L,current,request);
    ArgumentCaptor<AquacultureTask> captor=ArgumentCaptor.forClass(AquacultureTask.class);
    verify(tasks,times(2)).save(captor.capture());
    AquacultureTask next=captor.getAllValues().get(1);
    assertThat(item.isNextTaskCreated()).isTrue();
    assertThat(next.getDueAt()).isEqualTo(Instant.parse("2026-10-19T08:00:00Z"));
    assertThat(next.getRepeatIntervalDays()).isEqualTo(7);
    assertThat(next.getCompletedAt()).isNull();
    assertThat(response.getMessage()).contains("下一期");
  }

  @Test void recompletingRestoredRecurringTaskDoesNotCreateDuplicate() {
    AquacultureTaskController controller=new AquacultureTaskController(tasks,users);
    AuthUser current=new AuthUser(7L,"owner", Set.of(RoleName.USER));
    AquacultureTask item=new AquacultureTask(); item.setId(12L); item.setDueAt(Instant.now()); item.setRepeatIntervalDays(7); item.setNextTaskCreated(true);
    when(tasks.findByIdAndUserId(12L,7L)).thenReturn(Optional.of(item));
    when(tasks.save(any())).thenAnswer(invocation->invocation.getArgument(0));
    TaskCompletionRequest request=new TaskCompletionRequest(); request.setCompleted(true);

    controller.completion(12L,current,request);
    verify(tasks,times(1)).save(item);
  }

  @Test void overdueRecurringTaskSkipsMissedOccurrencesAndCreatesFutureTask() {
    AquacultureTaskController controller=new AquacultureTaskController(tasks,users);
    AuthUser current=new AuthUser(7L,"owner",Set.of(RoleName.USER));
    User owner=new User();owner.setId(7L);
    Instant originalDue=Instant.parse("2025-01-06T08:00:00Z");
    AquacultureTask item=new AquacultureTask();item.setId(21L);item.setUser(owner);item.setTitle("每周检测");item.setCategory("WATER_TEST");item.setDueAt(originalDue);item.setRepeatIntervalDays(7);
    when(tasks.findByIdAndUserId(21L,7L)).thenReturn(Optional.of(item));
    when(tasks.save(any())).thenAnswer(invocation->invocation.getArgument(0));
    TaskCompletionRequest request=new TaskCompletionRequest();request.setCompleted(true);
    Instant before=Instant.now();

    var response=controller.completion(21L,current,request);
    ArgumentCaptor<AquacultureTask> captor=ArgumentCaptor.forClass(AquacultureTask.class);
    verify(tasks,times(2)).save(captor.capture());
    AquacultureTask next=captor.getAllValues().get(1);
    long daysFromOriginal=java.time.temporal.ChronoUnit.DAYS.between(originalDue,next.getDueAt());

    assertThat(next.getDueAt()).isAfter(before);
    assertThat(daysFromOriginal).isPositive();
    assertThat(daysFromOriginal%7).isZero();
    assertThat(response.getData().getNextDueAt()).isEqualTo(next.getDueAt());
  }

  @Test void routinePlanCreatesSelectedRecurringTasksAndSkipsExistingPreset() {
    AquacultureTaskController controller=new AquacultureTaskController(tasks,users);
    AuthUser current=new AuthUser(7L,"owner",Set.of(RoleName.USER));
    User owner=new User();owner.setId(7L);
    when(users.findById(7L)).thenReturn(Optional.of(owner));
    when(tasks.existsByUserIdAndCompletedAtIsNullAndPondNameAndTitle(7L,"一号池","日常投喂")).thenReturn(true);
    when(tasks.existsByUserIdAndCompletedAtIsNullAndPondNameAndTitle(7L,"一号池","检测水质")).thenReturn(false);
    when(tasks.save(any())).thenAnswer(invocation->invocation.getArgument(0));
    RoutinePlanRequest request=new RoutinePlanRequest();request.setPondName(" 一号池 ");request.setStartAt(Instant.now().plus(1,java.time.temporal.ChronoUnit.HOURS));request.setPresets(Set.of("FEEDING","WATER_TEST"));

    var response=controller.createRoutinePlan(current,request);

    assertThat(response.getData().created()).hasSize(1);
    assertThat(response.getData().skippedPresets()).containsExactly("FEEDING");
    AquacultureTaskDto created=response.getData().created().get(0);
    assertThat(created.getTitle()).isEqualTo("检测水质");
    assertThat(created.getCategory()).isEqualTo("WATER_TEST");
    assertThat(created.getPondName()).isEqualTo("一号池");
    assertThat(created.getRepeatDays()).isEqualTo(7);
    verify(tasks,times(1)).save(any(AquacultureTask.class));
  }

  @Test void routinePlanRejectsPastStartBeforeWriting() {
    AquacultureTaskController controller=new AquacultureTaskController(tasks,users);
    AuthUser current=new AuthUser(7L,"owner",Set.of(RoleName.USER));
    RoutinePlanRequest request=new RoutinePlanRequest();request.setPondName("一号池");request.setStartAt(Instant.now().minus(1,java.time.temporal.ChronoUnit.HOURS));request.setPresets(Set.of("OBSERVATION"));

    assertThatThrownBy(()->controller.createRoutinePlan(current,request)).isInstanceOf(BizException.class).hasMessage("计划开始时间不能早于当前时间");
    verify(tasks,never()).save(any());
  }

  @Test void routinePlanRejectsAnonymousUserBeforeWriting() {
    AquacultureTaskController controller=new AquacultureTaskController(tasks,users);
    RoutinePlanRequest request=new RoutinePlanRequest();request.setPondName("一号池");request.setStartAt(Instant.now().plusSeconds(3600));request.setPresets(Set.of("OBSERVATION"));

    assertThatThrownBy(()->controller.createRoutinePlan(null,request)).isInstanceOf(BizException.class).hasMessage("未登录");
    verify(tasks,never()).save(any());
  }
}
