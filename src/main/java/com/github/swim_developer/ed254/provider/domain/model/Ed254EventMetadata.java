package com.github.swim_developer.ed254.provider.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class Ed254EventMetadata {

    private final String aerodromeDesignator;
    private final String messageType;
    private final Instant publicationTime;
    private final Instant creationTime;
}
