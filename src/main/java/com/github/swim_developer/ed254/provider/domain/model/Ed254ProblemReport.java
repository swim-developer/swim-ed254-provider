package com.github.swim_developer.ed254.provider.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ed254ProblemReport {

    private UUID id;
    private String validationResultType;
    private String errorDetails;

    @Builder.Default
    private Instant reportedAt = Instant.now();

    private String reportedBy;
}
