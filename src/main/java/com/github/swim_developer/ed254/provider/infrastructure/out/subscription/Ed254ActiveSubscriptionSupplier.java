package com.github.swim_developer.ed254.provider.infrastructure.out.subscription;

import com.github.swim_developer.ed254.provider.domain.model.Ed254Subscription;
import com.github.swim_developer.ed254.provider.infrastructure.out.persistence.Ed254SubscriptionRepository;
import com.github.swim_developer.framework.domain.model.ActiveSubscriptionInfo;
import com.github.swim_developer.framework.domain.model.SubscriptionStatus;
import com.github.swim_developer.framework.application.port.out.ActiveSubscriptionSupplier;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class Ed254ActiveSubscriptionSupplier implements ActiveSubscriptionSupplier {

    private final Ed254SubscriptionRepository subscriptionRepository;

    @Inject
    public Ed254ActiveSubscriptionSupplier(Ed254SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public List<ActiveSubscriptionInfo> getActiveSubscriptions() {
        return subscriptionRepository.findByStatuses(SubscriptionStatus.ACTIVE, SubscriptionStatus.PAUSED).stream()
                .map(this::toInfo)
                .toList();
    }

    private ActiveSubscriptionInfo toInfo(Ed254Subscription sub) {
        return new ActiveSubscriptionInfo(
                sub.getSubscriptionId(),
                sub.getQueue(),
                sub.getStatus().name()
        );
    }
}
