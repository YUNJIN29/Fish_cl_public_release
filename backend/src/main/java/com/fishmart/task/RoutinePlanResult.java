package com.fishmart.task;

import java.util.List;

public record RoutinePlanResult(List<AquacultureTaskDto> created, List<String> skippedPresets) {}
