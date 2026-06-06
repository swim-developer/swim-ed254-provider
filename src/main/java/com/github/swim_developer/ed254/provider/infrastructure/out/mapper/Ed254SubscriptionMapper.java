package com.github.swim_developer.ed254.provider.infrastructure.out.mapper;

import com.github.swim_developer.ed254.provider.application.port.out.Ed254SubscriptionMappingPort;
import com.github.swim_developer.ed254.provider.domain.model.Ed254Subscription;
import com.github.swim_developer.ed254.provider.domain.model.Ed254SubscriptionCommand;
import com.github.swim_developer.ed254.provider.domain.model.Ed254SubscriptionResult;
import com.github.swim_developer.ed254.provider.domain.model.Ed254SupplementaryData;
import com.github.swim_developer.framework.domain.model.SubscriptionStatus;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

@ApplicationScoped
public class Ed254SubscriptionMapper implements Ed254SubscriptionMappingPort {

    @Override
    public Ed254Subscription toEntity(Ed254SubscriptionCommand command, String userId,
                                      String queueName, Duration defaultTtl) {
        Instant now = Instant.now();

        var builder = Ed254Subscription.builder()
                .userId(userId)
                .queue(queueName)
                .status(SubscriptionStatus.PAUSED)
                .qos(command.qos())
                .aerodromes(new ArrayList<>(command.extractAerodromeDesignators()))
                .pointNames(new ArrayList<>(command.extractPointNames()))
                .runwayDirections(new ArrayList<>(command.extractRunwayDesignators()))
                .createdAt(now)
                .subscriptionEnd(now.plus(defaultTtl));

        Ed254SupplementaryData sup = command.supplementaryData();
        if (sup != null) {
            builder.supDelay(sup.isDelay())
                    .supLandingSequencePosition(sup.isLandingSequencePosition())
                    .supAmanStrategy(sup.isAmanStrategy())
                    .supDepartureAerodrome(sup.isDepartureAerodrome())
                    .supProposedProcedure(sup.isProposedProcedure());
        }

        return builder.build();
    }

    @Override
    public Ed254SubscriptionResult toResult(Ed254Subscription entity, String providerName) {
        return new Ed254SubscriptionResult(
                entity.getSubscriptionId(),
                "SUBSCRIPTION_SUCCESSFUL",
                null,
                entity.getQueue(),
                entity.getStatus().name(),
                entity.getSubscriptionEnd(),
                providerName,
                entity.getQueue() + "-heartbeat"
        );
    }
}
