package com.github.swim_developer.ed254.provider.application.port.out;

import com.github.swim_developer.ed254.provider.domain.model.Ed254Subscription;
import com.github.swim_developer.ed254.provider.domain.model.Ed254SubscriptionCommand;
import com.github.swim_developer.ed254.provider.domain.model.Ed254SubscriptionResult;

import java.time.Duration;

public interface Ed254SubscriptionMappingPort {

    Ed254Subscription toEntity(Ed254SubscriptionCommand command, String userId, String queueName, Duration defaultTtl);

    Ed254SubscriptionResult toResult(Ed254Subscription entity, String providerName);
}
