package com.github.swim_developer.ed254.provider.infrastructure.out.persistence;

import com.github.swim_developer.ed254.provider.domain.model.Ed254EventEntity;
import com.github.swim_developer.ed254.provider.domain.model.Ed254FailedDeliveryEntity;
import com.github.swim_developer.ed254.provider.domain.model.Ed254ProblemReport;
import com.github.swim_developer.ed254.provider.domain.model.Ed254Subscription;
import com.github.swim_developer.ed254.provider.infrastructure.out.persistence.entity.Ed254EventJpaEntity;
import com.github.swim_developer.ed254.provider.infrastructure.out.persistence.entity.Ed254FailedDeliveryJpaEntity;
import com.github.swim_developer.ed254.provider.infrastructure.out.persistence.entity.Ed254ProblemReportJpaEntity;
import com.github.swim_developer.ed254.provider.infrastructure.out.persistence.entity.Ed254SubscriptionJpaEntity;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Ed254ProviderPersistenceMapper {

    public Ed254SubscriptionJpaEntity toJpa(Ed254Subscription domain) {
        return Ed254SubscriptionJpaEntity.builder()
                .subscriptionId(domain.getSubscriptionId())
                .userId(domain.getUserId())
                .queue(domain.getQueue())
                .status(domain.getStatus())
                .qos(domain.getQos())
                .aerodromes(domain.getAerodromes())
                .pointNames(domain.getPointNames())
                .runwayDirections(domain.getRunwayDirections())
                .supDelay(domain.isSupDelay())
                .supLandingSequencePosition(domain.isSupLandingSequencePosition())
                .supAmanStrategy(domain.isSupAmanStrategy())
                .supDepartureAerodrome(domain.isSupDepartureAerodrome())
                .supProposedProcedure(domain.isSupProposedProcedure())
                .flightSelectorsJson(domain.getFlightSelectorsJson())
                .subscriptionHash(domain.getSubscriptionHash())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .subscriptionEnd(domain.getSubscriptionEnd())
                .build();
    }

    public Ed254Subscription toDomain(Ed254SubscriptionJpaEntity jpa) {
        return Ed254Subscription.builder()
                .subscriptionId(jpa.getSubscriptionId())
                .userId(jpa.getUserId())
                .queue(jpa.getQueue())
                .status(jpa.getStatus())
                .qos(jpa.getQos())
                .aerodromes(jpa.getAerodromes())
                .pointNames(jpa.getPointNames())
                .runwayDirections(jpa.getRunwayDirections())
                .supDelay(jpa.isSupDelay())
                .supLandingSequencePosition(jpa.isSupLandingSequencePosition())
                .supAmanStrategy(jpa.isSupAmanStrategy())
                .supDepartureAerodrome(jpa.isSupDepartureAerodrome())
                .supProposedProcedure(jpa.isSupProposedProcedure())
                .flightSelectorsJson(jpa.getFlightSelectorsJson())
                .subscriptionHash(jpa.getSubscriptionHash())
                .createdAt(jpa.getCreatedAt())
                .updatedAt(jpa.getUpdatedAt())
                .subscriptionEnd(jpa.getSubscriptionEnd())
                .build();
    }

    public Ed254EventJpaEntity toJpa(Ed254EventEntity domain) {
        return Ed254EventJpaEntity.builder()
                .eventId(domain.getEventId())
                .aerodromeDesignator(domain.getAerodromeDesignator())
                .messageType(domain.getMessageType())
                .publicationTime(domain.getPublicationTime())
                .creationTime(domain.getCreationTime())
                .rawPayload(domain.getRawPayload())
                .status(domain.getStatus())
                .receivedAt(domain.getReceivedAt())
                .processedAt(domain.getProcessedAt())
                .deliveredCount(domain.getDeliveredCount())
                .retryCount(domain.getRetryCount())
                .firstMessageAfterServiceOutage(domain.isFirstMessageAfterServiceOutage())
                .build();
    }

    public Ed254EventEntity toDomain(Ed254EventJpaEntity jpa) {
        return Ed254EventEntity.builder()
                .eventId(jpa.getEventId())
                .aerodromeDesignator(jpa.getAerodromeDesignator())
                .messageType(jpa.getMessageType())
                .publicationTime(jpa.getPublicationTime())
                .creationTime(jpa.getCreationTime())
                .rawPayload(jpa.getRawPayload())
                .status(jpa.getStatus())
                .receivedAt(jpa.getReceivedAt())
                .processedAt(jpa.getProcessedAt())
                .deliveredCount(jpa.getDeliveredCount())
                .retryCount(jpa.getRetryCount())
                .firstMessageAfterServiceOutage(jpa.isFirstMessageAfterServiceOutage())
                .build();
    }

    public Ed254FailedDeliveryJpaEntity toJpa(Ed254FailedDeliveryEntity domain) {
        return Ed254FailedDeliveryJpaEntity.builder()
                .id(domain.getId())
                .eventId(domain.getEventId())
                .subscriptionId(domain.getSubscriptionId())
                .queue(domain.getQueue())
                .errorMessage(domain.getErrorMessage())
                .retryCount(domain.getRetryCount())
                .resolved(domain.isResolved())
                .createdAt(domain.getCreatedAt())
                .resolvedAt(domain.getResolvedAt())
                .build();
    }

    public Ed254FailedDeliveryEntity toDomain(Ed254FailedDeliveryJpaEntity jpa) {
        return Ed254FailedDeliveryEntity.builder()
                .id(jpa.getId())
                .eventId(jpa.getEventId())
                .subscriptionId(jpa.getSubscriptionId())
                .queue(jpa.getQueue())
                .errorMessage(jpa.getErrorMessage())
                .retryCount(jpa.getRetryCount())
                .resolved(jpa.isResolved())
                .createdAt(jpa.getCreatedAt())
                .resolvedAt(jpa.getResolvedAt())
                .build();
    }

    public Ed254ProblemReportJpaEntity toJpa(Ed254ProblemReport domain) {
        return Ed254ProblemReportJpaEntity.builder()
                .id(domain.getId())
                .validationResultType(domain.getValidationResultType())
                .errorDetails(domain.getErrorDetails())
                .reportedAt(domain.getReportedAt())
                .reportedBy(domain.getReportedBy())
                .build();
    }

    public Ed254ProblemReport toDomain(Ed254ProblemReportJpaEntity jpa) {
        return Ed254ProblemReport.builder()
                .id(jpa.getId())
                .validationResultType(jpa.getValidationResultType())
                .errorDetails(jpa.getErrorDetails())
                .reportedAt(jpa.getReportedAt())
                .reportedBy(jpa.getReportedBy())
                .build();
    }
}
