package com.github.swim_developer.ed254.provider.domain.model;

import java.time.Instant;

public record EventQueryFilters(
        String aerodromeDesignator,
        String messageType,
        Instant startTime,
        Instant endTime,
        int startIndex,
        int count
) {
}
