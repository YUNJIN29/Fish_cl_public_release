package com.fishmart.task;

import lombok.Data;

import java.time.Instant;

@Data
public class AquacultureTaskDto {
  private Long id; private String title; private String category; private String pondName; private Instant dueAt;
  private String notes; private Integer repeatDays; private Instant completedAt; private Instant createdAt; private Instant updatedAt;
  private Instant nextDueAt;

  public static AquacultureTaskDto from(AquacultureTask item) {
    AquacultureTaskDto dto = new AquacultureTaskDto(); dto.id=item.getId(); dto.title=item.getTitle(); dto.category=item.getCategory();
    dto.pondName=item.getPondName(); dto.dueAt=item.getDueAt(); dto.notes=item.getNotes(); dto.repeatDays=item.getRepeatIntervalDays(); dto.completedAt=item.getCompletedAt();
    dto.createdAt=item.getCreatedAt(); dto.updatedAt=item.getUpdatedAt(); return dto;
  }
}
