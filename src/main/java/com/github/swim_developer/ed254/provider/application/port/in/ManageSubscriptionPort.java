package com.github.swim_developer.ed254.provider.application.port.in;

import com.github.swim_developer.ed254.provider.domain.model.Ed254SubscriptionCommand;
import com.github.swim_developer.ed254.provider.domain.model.Ed254SubscriptionResult;
import com.github.swim_developer.framework.domain.model.SubscriptionStatus;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

public interface ManageSubscriptionPort {

    Ed254SubscriptionResult createSubscription(Ed254SubscriptionCommand command);

    List<Ed254SubscriptionResult> listSubscriptions();

    Ed254SubscriptionResult getSubscription(UUID subscriptionId);

    Ed254SubscriptionResult updateStatus(UUID subscriptionId, SubscriptionStatus newStatus);

    Ed254SubscriptionResult renewSubscription(UUID subscriptionId, Duration extensionTtl);

    void deleteSubscription(UUID subscriptionReference);
}
