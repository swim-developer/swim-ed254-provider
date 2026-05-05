package com.github.swim_developer.ed254.provider.domain.port.out;

import com.github.swim_developer.ed254.provider.domain.model.Ed254Subscription;
import com.github.swim_developer.framework.domain.model.SubscriptionStatus;
import com.github.swim_developer.framework.application.port.out.SwimSubscriptionRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface Ed254SubscriptionStore extends SwimSubscriptionRepository<Ed254Subscription> {

    Optional<Ed254Subscription> findSubscriptionById(UUID id);

    Optional<Ed254Subscription> findBySubscriptionHash(String hash);

    List<Ed254Subscription> findAllSubscriptions();

    List<Ed254Subscription> findByStatus(SubscriptionStatus status);
}
