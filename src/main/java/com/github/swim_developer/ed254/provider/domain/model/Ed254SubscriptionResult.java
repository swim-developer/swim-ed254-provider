package com.github.swim_developer.ed254.provider.domain.model;

import java.time.Instant;
import java.util.UUID;

public record Ed254SubscriptionResult(
        UUID subscriptionId,
        String subscriptionResult,
        String errorReason,
        String queueName,
        String subscriptionStatus,
        Instant subscriptionEnd,
        String providerName,
        String heartbeatQueue) {
}
