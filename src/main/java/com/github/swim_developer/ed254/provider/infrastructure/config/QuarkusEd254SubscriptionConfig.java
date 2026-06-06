package com.github.swim_developer.ed254.provider.infrastructure.config;

import com.github.swim_developer.ed254.provider.application.port.in.Ed254SubscriptionConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;

@ApplicationScoped
public class QuarkusEd254SubscriptionConfig implements Ed254SubscriptionConfig {

    private final Duration defaultTtl;
    private final String providerName;

    @Inject
    public QuarkusEd254SubscriptionConfig(
            @ConfigProperty(name = "swim.subscription.expiry.default-ttl", defaultValue = "24h")
            Duration defaultTtl,
            @ConfigProperty(name = "swim.provider.name", defaultValue = "SWIM-ED254-Provider")
            String providerName) {
        this.defaultTtl = defaultTtl;
        this.providerName = providerName;
    }

    @Override
    public Duration defaultTtl() {
        return defaultTtl;
    }

    @Override
    public String providerName() {
        return providerName;
    }
}
