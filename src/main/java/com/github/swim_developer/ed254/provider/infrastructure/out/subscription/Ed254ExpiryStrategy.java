package com.github.swim_developer.ed254.provider.infrastructure.out.subscription;

import com.github.swim_developer.ed254.provider.domain.model.Ed254Subscription;
import com.github.swim_developer.ed254.provider.application.usecase.Ed254SubscriptionUseCase;
import com.github.swim_developer.ed254.provider.infrastructure.out.persistence.Ed254SubscriptionRepository;
import com.github.swim_developer.framework.domain.model.SubscriptionStatus;
import com.github.swim_developer.framework.domain.model.SubscriptionExpiry;
import com.github.swim_developer.framework.application.port.out.SubscriptionExpiryStrategy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@ApplicationScoped
public class Ed254ExpiryStrategy implements SubscriptionExpiryStrategy {

    private final Ed254SubscriptionRepository subscriptionRepository;
    private final Ed254SubscriptionUseCase subscriptionService;

    @Inject
    public Ed254ExpiryStrategy(Ed254SubscriptionRepository subscriptionRepository,
            Ed254SubscriptionUseCase subscriptionService) {
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionService = subscriptionService;
    }

    @Override
    public List<SubscriptionExpiry> findExpiredSubscriptions(Instant now) {
        return subscriptionRepository.findBySubscriptionEndBefore(now)
                .stream()
                .map(this::toSubscriptionExpiry)
                .toList();
    }

    @Override
    public List<SubscriptionExpiry> findTerminatedSubscriptionsToPurge(Instant threshold) {
        return subscriptionRepository.findByStatusAndUpdatedAtBefore(SubscriptionStatus.TERMINATED, threshold)
                .stream()
                .map(this::toSubscriptionExpiry)
                .toList();
    }

    @Override
    public void terminateSubscription(String subscriptionId) {
        subscriptionService.terminateSubscription(UUID.fromString(subscriptionId));
    }

    @Override
    public void purgeSubscription(String subscriptionId) {
        subscriptionService.purgeSubscription(UUID.fromString(subscriptionId));
    }

    private SubscriptionExpiry toSubscriptionExpiry(Ed254Subscription subscription) {
        return new SubscriptionExpiry(
                subscription.getSubscriptionId().toString(),
                subscription.getSubscriptionEnd(),
                subscription.getStatus().name()
        );
    }
}
