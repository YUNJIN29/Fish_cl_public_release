package com.fishmart.task;

public record TaskSummaryDto(
  long openCount,
  long overdueCount,
  long dueSoonCount,
  long recurringCount
) {}
