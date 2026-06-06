package com.github.swim_developer.ed254.provider.application.port.in;

import java.time.Duration;

public interface Ed254SubscriptionConfig {
    Duration defaultTtl();
    String providerName();
}
