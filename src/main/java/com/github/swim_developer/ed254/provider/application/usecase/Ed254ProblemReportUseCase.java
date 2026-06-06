package com.github.swim_developer.ed254.provider.application.usecase;

import com.github.swim_developer.ed254.provider.domain.model.Ed254ProblemReport;
import com.github.swim_developer.ed254.provider.domain.port.out.Ed254ProblemReportPort;
import com.github.swim_developer.framework.domain.model.DataValidationResult;
import com.github.swim_developer.framework.domain.model.ValidationResultType;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class Ed254ProblemReportUseCase {

    private final Ed254ProblemReportPort repository;
    private final MeterRegistry meterRegistry;

    @Inject
    public Ed254ProblemReportUseCase(Ed254ProblemReportPort repository, MeterRegistry meterRegistry) {
        this.repository = repository;
        this.meterRegistry = meterRegistry;
    }

    @Transactional
    public void processProblemReport(DataValidationResult request, String reportedBy) {
        logProblemReport(request);
        persistProblemReport(request, reportedBy);
        recordMetrics(request);
    }

    private void logProblemReport(DataValidationResult request) {
        ValidationResultType type = request.dataValResult();
        int errorCount = request.errorReport() != null ? request.errorReport().size() : 0;

        if (type == ValidationResultType.SEQUENCE_GAPS || type == ValidationResultType.DATA_INVALID) {
            log.warn("CommunicateProblems received: type={}, errors={}", type, errorCount);
        } else {
            log.error("CommunicateProblems received: type={}, errors={}", type, errorCount);
        }

        if (request.errorReport() != null) {
            request.errorReport().forEach(detail ->
                    log.info("  Error detail: code={}, field={}, message={}",
                            detail.errorCode(), detail.erroneousFieldName(), detail.errorMessage()));
        }
    }

    private void persistProblemReport(DataValidationResult request, String reportedBy) {
        String errorDetails = request.errorReport() != null
                ? request.errorReport().toString()
                : null;

        Ed254ProblemReport report = Ed254ProblemReport.builder()
                .validationResultType(request.dataValResult().name())
                .errorDetails(errorDetails)
                .reportedBy(reportedBy)
                .build();

        repository.persist(report);
        log.debug("Problem report persisted: id={}, type={}", report.getId(), report.getValidationResultType());
    }

    private void recordMetrics(DataValidationResult request) {
        meterRegistry.counter("ed254_problems_reported",
                "type", request.dataValResult().name()).increment();
    }
}
