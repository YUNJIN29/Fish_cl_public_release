package com.fishmart.waterquality;

import java.util.List;

public record WaterQualityPondOverviewDto(
    String pondName,
    WaterQualityDto latest,
    boolean hasTarget,
    List<String> deviations,
    long ageDays,
    boolean stale
) {}
