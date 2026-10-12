package com.fishmart.task;

import com.fishmart.common.ApiResponse;
import com.fishmart.common.BizException;
import com.fishmart.security.AuthUser;
import com.fishmart.user.UserRepository;
import com.fishmart.user.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class AquacultureTaskController {
  private static final Map<String, RoutinePreset> ROUTINE_PRESETS = routinePresets();
  private final AquacultureTaskRepository tasks;
  private final UserRepository users;
  public AquacultureTaskController(AquacultureTaskRepository tasks, UserRepository users) { this.tasks=tasks; this.users=users; }

  @GetMapping
  public ApiResponse<Page<AquacultureTaskDto>> list(@AuthenticationPrincipal AuthUser authUser,
                                                     @RequestParam(defaultValue="OPEN") String status,
                                                     @RequestParam(defaultValue="ALL") String scope,
                                                     @RequestParam(defaultValue="") String q,
                                                     @RequestParam(defaultValue="") String pondName,
                                                     @RequestParam(defaultValue="") String category,
                                                     @RequestParam(defaultValue="0") int page,
                                                     @RequestParam(defaultValue="20") int size) {
    requireUser(authUser);
    String normalizedStatus=normalized(status,"OPEN","COMPLETED","ALL");
    String normalizedScope=normalized(scope,"ALL","OVERDUE","DUE_SOON","RECURRING");
    String normalizedCategory=category == null ? "" : category.trim().toUpperCase();
    if (!normalizedCategory.isEmpty() && !normalizedCategory.matches("FEEDING|WATER_TEST|WATER_CHANGE|MEDICATION|OBSERVATION|OTHER")) throw new BizException("待办分类无效");
    String keyword=limited(q,80), pond=limited(pondName,64);
    Sort sort=normalizedStatus.equals("COMPLETED")?Sort.by(Sort.Direction.DESC,"completedAt"):normalizedStatus.equals("ALL")?Sort.by(Sort.Direction.DESC,"dueAt"):Sort.by(Sort.Direction.ASC,"dueAt");
    PageRequest pageable=PageRequest.of(Math.max(0,page),Math.max(1,Math.min(size,50)),sort);
    Instant now=Instant.now();
    boolean filtered=!keyword.isEmpty()||!pond.isEmpty()||!normalizedCategory.isEmpty();
    Page<AquacultureTask> result=filtered?tasks.search(authUser.getId(),normalizedStatus,normalizedScope,normalizedCategory,pond,keyword,now,now.plus(24,ChronoUnit.HOURS),pageable):switch(normalizedStatus) {
      case "COMPLETED" -> tasks.findByUserIdAndCompletedAtIsNotNullOrderByCompletedAtDesc(authUser.getId(),pageable);
      case "ALL" -> tasks.findByUserIdOrderByDueAtDesc(authUser.getId(),pageable);
      default -> switch(scope.toUpperCase()) {
        case "OVERDUE" -> tasks.findByUserIdAndCompletedAtIsNullAndDueAtBeforeOrderByDueAtAsc(authUser.getId(),now,pageable);
        case "DUE_SOON" -> tasks.findByUserIdAndCompletedAtIsNullAndDueAtGreaterThanEqualAndDueAtBeforeOrderByDueAtAsc(authUser.getId(),now,now.plus(24,ChronoUnit.HOURS),pageable);
        case "RECURRING" -> tasks.findByUserIdAndCompletedAtIsNullAndRepeatIntervalDaysIsNotNullOrderByDueAtAsc(authUser.getId(),pageable);
        default -> tasks.findByUserIdAndCompletedAtIsNullOrderByDueAtAsc(authUser.getId(),pageable);
      };
    };
    return ApiResponse.ok(result.map(AquacultureTaskDto::from));
  }

  @GetMapping("/ponds")
  public ApiResponse<List<String>> pondNames(@AuthenticationPrincipal AuthUser authUser) {
    requireUser(authUser);
    return ApiResponse.ok(tasks.findDistinctPondNames(authUser.getId()));
  }

  @GetMapping("/summary")
  public ApiResponse<TaskSummaryDto> summary(@AuthenticationPrincipal AuthUser authUser) {
    requireUser(authUser);
    Instant now=Instant.now();
    Long userId=authUser.getId();
    return ApiResponse.ok(new TaskSummaryDto(
      tasks.countByUserIdAndCompletedAtIsNull(userId),
      tasks.countByUserIdAndCompletedAtIsNullAndDueAtBefore(userId,now),
      tasks.countByUserIdAndCompletedAtIsNullAndDueAtGreaterThanEqualAndDueAtBefore(userId,now,now.plus(24, ChronoUnit.HOURS)),
      tasks.countByUserIdAndCompletedAtIsNullAndRepeatIntervalDaysIsNotNull(userId)
    ));
  }

  @GetMapping(value="/calendar.ics",produces="text/calendar;charset=UTF-8")
  public ResponseEntity<byte[]> exportCalendar(@AuthenticationPrincipal AuthUser authUser) {
    requireUser(authUser);
    String calendar=buildCalendar(tasks.findByUserIdAndCompletedAtIsNullOrderByDueAtAsc(authUser.getId()));
    return ResponseEntity.ok()
      .contentType(MediaType.parseMediaType("text/calendar;charset=UTF-8"))
      .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"aquaculture-tasks.ics\"")
      .body(calendar.getBytes(StandardCharsets.UTF_8));
  }

  @PostMapping
  public ApiResponse<AquacultureTaskDto> create(@AuthenticationPrincipal AuthUser authUser,@Valid @RequestBody AquacultureTaskRequest request) {
    requireUser(authUser); AquacultureTask item=new AquacultureTask(); item.setUser(users.findById(authUser.getId()).orElseThrow(()->new BizException("用户不存在"))); apply(item,request);
    return ApiResponse.ok("待办已创建",AquacultureTaskDto.from(tasks.save(item)));
  }

  @PostMapping("/routine-plan")
  @Transactional
  public ApiResponse<RoutinePlanResult> createRoutinePlan(@AuthenticationPrincipal AuthUser authUser,
                                                           @Valid @RequestBody RoutinePlanRequest request) {
    requireUser(authUser);
    Instant now = Instant.now();
    if (request.getStartAt().isBefore(now.minus(5, ChronoUnit.MINUTES))) {
      throw new BizException("计划开始时间不能早于当前时间");
    }
    User owner = users.findById(authUser.getId()).orElseThrow(() -> new BizException("用户不存在"));
    String pondName = request.getPondName().trim();
    List<AquacultureTaskDto> created = new ArrayList<>();
    List<String> skipped = new ArrayList<>();
    for (String key : ROUTINE_PRESETS.keySet()) {
      if (!request.getPresets().contains(key)) continue;
      RoutinePreset preset = ROUTINE_PRESETS.get(key);
      if (tasks.existsByUserIdAndCompletedAtIsNullAndPondNameAndTitle(authUser.getId(), pondName, preset.title())) {
        skipped.add(key);
        continue;
      }
      AquacultureTask item = new AquacultureTask();
      item.setUser(owner);
      item.setTitle(preset.title());
      item.setCategory(preset.category());
      item.setPondName(pondName);
      item.setDueAt(request.getStartAt());
      item.setNotes(preset.notes());
      item.setRepeatIntervalDays(preset.repeatDays());
      created.add(AquacultureTaskDto.from(tasks.save(item)));
    }
    if (created.isEmpty()) throw new BizException("所选日常计划均已存在，无需重复创建");
    String message = skipped.isEmpty() ? "日常计划已创建" : "日常计划已创建，已跳过重复项目";
    return ApiResponse.ok(message, new RoutinePlanResult(created, skipped));
  }

  @PutMapping("/{id}")
  public ApiResponse<AquacultureTaskDto> update(@PathVariable Long id,@AuthenticationPrincipal AuthUser authUser,@Valid @RequestBody AquacultureTaskRequest request) {
    AquacultureTask item=owned(id,authUser); apply(item,request); return ApiResponse.ok("待办已更新",AquacultureTaskDto.from(tasks.save(item)));
  }

  @PatchMapping("/{id}/completion")
  @Transactional
  public ApiResponse<AquacultureTaskDto> completion(@PathVariable Long id,@AuthenticationPrincipal AuthUser authUser,@Valid @RequestBody TaskCompletionRequest request) {
    AquacultureTask item=owned(id,authUser); boolean completing=Boolean.TRUE.equals(request.getCompleted()); Instant now=Instant.now(); item.setCompletedAt(completing?now:null); item.setUpdatedAt(now);
    boolean createNext=completing&&item.getRepeatIntervalDays()!=null&&!item.isNextTaskCreated();
    Instant nextDueAt=null;
    if(createNext){item.setNextTaskCreated(true);tasks.save(item);AquacultureTask next=nextOccurrence(item,now);tasks.save(next);nextDueAt=next.getDueAt();}
    else tasks.save(item);
    AquacultureTaskDto response=AquacultureTaskDto.from(item);response.setNextDueAt(nextDueAt);
    return ApiResponse.ok(createNext?"待办已完成，下一期已自动安排":completing?"待办已完成":"待办已恢复",response);
  }

  @PatchMapping("/{id}/reschedule")
  public ApiResponse<AquacultureTaskDto> reschedule(@PathVariable Long id,@AuthenticationPrincipal AuthUser authUser,@Valid @RequestBody TaskRescheduleRequest request) {
    AquacultureTask item=owned(id,authUser);
    if(item.getCompletedAt()!=null)throw new BizException("已完成待办不能顺延");
    Instant now=Instant.now();
    Instant base=item.getDueAt().isBefore(now)?now:item.getDueAt();
    item.setDueAt(base.plus(request.getDays(),ChronoUnit.DAYS));
    item.setUpdatedAt(now);
    return ApiResponse.ok("待办已顺延 "+request.getDays()+" 天",AquacultureTaskDto.from(tasks.save(item)));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable Long id,@AuthenticationPrincipal AuthUser authUser) { AquacultureTask item=owned(id,authUser);tasks.delete(item);return ApiResponse.ok("待办已删除",null); }

  private AquacultureTask owned(Long id,AuthUser authUser){requireUser(authUser);return tasks.findByIdAndUserId(id,authUser.getId()).orElseThrow(()->new BizException("待办不存在"));}
  private void requireUser(AuthUser authUser){if(authUser==null)throw new BizException("未登录");}
  private void apply(AquacultureTask item,AquacultureTaskRequest request){item.setTitle(request.getTitle().trim());item.setCategory(request.getCategory());item.setPondName(trim(request.getPondName()));item.setDueAt(request.getDueAt());item.setNotes(trim(request.getNotes()));item.setRepeatIntervalDays(request.getRepeatDays());item.setUpdatedAt(Instant.now());}
  private AquacultureTask nextOccurrence(AquacultureTask source,Instant now){int repeatDays=source.getRepeatIntervalDays();long elapsedDays=Math.max(0,ChronoUnit.DAYS.between(source.getDueAt(),now));long intervals=elapsedDays/repeatDays+1;Instant nextDueAt=source.getDueAt().plus(intervals*repeatDays,ChronoUnit.DAYS);if(!nextDueAt.isAfter(now))nextDueAt=nextDueAt.plus(repeatDays,ChronoUnit.DAYS);AquacultureTask next=new AquacultureTask();next.setUser(source.getUser());next.setTitle(source.getTitle());next.setCategory(source.getCategory());next.setPondName(source.getPondName());next.setDueAt(nextDueAt);next.setNotes(source.getNotes());next.setRepeatIntervalDays(repeatDays);return next;}
  private String buildCalendar(java.util.List<AquacultureTask> items){StringBuilder out=new StringBuilder("BEGIN:VCALENDAR\r\nVERSION:2.0\r\nPRODID:-//Aquatic Lab//Aquaculture Tasks//ZH-CN\r\nCALSCALE:GREGORIAN\r\nMETHOD:PUBLISH\r\n");for(AquacultureTask item:items){out.append("BEGIN:VEVENT\r\nUID:task-").append(item.getId()).append("@example.com\r\nDTSTAMP:").append(icsTime(Instant.now())).append("\r\nDTSTART:").append(icsTime(item.getDueAt())).append("\r\nDTEND:").append(icsTime(item.getDueAt().plus(30,ChronoUnit.MINUTES))).append("\r\nSUMMARY:").append(icsText(item.getTitle())).append("\r\n");if(item.getPondName()!=null)out.append("LOCATION:").append(icsText(item.getPondName())).append("\r\n");if(item.getNotes()!=null)out.append("DESCRIPTION:").append(icsText(item.getNotes())).append("\r\n");out.append("END:VEVENT\r\n");}return out.append("END:VCALENDAR\r\n").toString();}
  private String icsTime(Instant value){return DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC).format(value);}
  private String icsText(String value){return value.replace("\\","\\\\").replace(";","\\;").replace(",","\\,").replace("\r","").replace("\n","\\n");}
  private String trim(String value){return value==null||value.isBlank()?null:value.trim();}
  private String limited(String value,int max){if(value==null)return "";String trimmed=value.trim();return trimmed.length()>max?trimmed.substring(0,max):trimmed;}
  private String normalized(String value,String... allowed){String normalized=value==null?allowed[0]:value.trim().toUpperCase();for(String item:allowed)if(item.equals(normalized))return normalized;throw new BizException("筛选参数无效");}

  private static Map<String, RoutinePreset> routinePresets() {
    Map<String, RoutinePreset> values = new LinkedHashMap<>();
    values.put("FEEDING", new RoutinePreset("日常投喂", "FEEDING", 1, "根据天气、水温和摄食情况调整投喂量。"));
    values.put("OBSERVATION", new RoutinePreset("巡塘观察", "OBSERVATION", 1, "观察摄食、活动、浮头、体表和死亡情况。"));
    values.put("WATER_TEST", new RoutinePreset("检测水质", "WATER_TEST", 7, "记录水温、pH、溶氧、氨氮和亚硝酸盐。"));
    values.put("EQUIPMENT_CHECK", new RoutinePreset("检查增氧与供电设备", "OTHER", 7, "检查增氧机、线路和备用供电是否正常。"));
    return java.util.Collections.unmodifiableMap(values);
  }

  private record RoutinePreset(String title, String category, int repeatDays, String notes) {}
}
