package com.github.swim_developer.ed254.provider.infrastructure.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ed254_problem_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ed254ProblemReportJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "validation_result_type", nullable = false)
    private String validationResultType;

    @Column(name = "error_details", columnDefinition = "TEXT")
    private String errorDetails;

    @Column(name = "reported_at", nullable = false)
    @Builder.Default
    private Instant reportedAt = Instant.now();

    @Column(name = "reported_by")
    private String reportedBy;
}
