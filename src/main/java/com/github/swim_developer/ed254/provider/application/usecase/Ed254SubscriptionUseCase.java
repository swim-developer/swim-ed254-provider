package com.github.swim_developer.ed254.provider.application.usecase;

import com.github.swim_developer.ed254.provider.application.port.in.Ed254SubscriptionConfig;
import com.github.swim_developer.ed254.provider.application.port.out.Ed254SubscriptionHashPort;
import com.github.swim_developer.ed254.provider.application.port.out.Ed254SubscriptionMappingPort;
import com.github.swim_developer.ed254.provider.domain.model.Ed254Subscription;
import com.github.swim_developer.ed254.provider.domain.model.Ed254SubscriptionCommand;
import com.github.swim_developer.ed254.provider.domain.model.Ed254SubscriptionResult;
import com.github.swim_developer.ed254.provider.application.port.in.ManageSubscriptionPort;
import com.github.swim_developer.ed254.provider.domain.port.out.Ed254SubscriptionStore;
import com.github.swim_developer.framework.application.port.out.SwimSubscriptionQueuePort;
import com.github.swim_developer.framework.provider.application.subscription.AbstractProviderSubscriptionService;
import com.github.swim_developer.framework.application.port.out.SwimSecurityContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Slf4j
@ApplicationScoped
public class Ed254SubscriptionUseCase
        extends AbstractProviderSubscriptionService<Ed254Subscription, Ed254SubscriptionCommand, Ed254SubscriptionResult>
        implements ManageSubscriptionPort {

    private static final String QUEUE_PREFIX = "ed254-";

    private final Ed254SubscriptionHashPort hashCalculator;
    private final Ed254SubscriptionMappingPort subscriptionMapper;
    private final Ed254SubscriptionConfig config;

    protected Ed254SubscriptionUseCase() {
        this(null, null, null, null, null, null);
    }

    @Inject
    public Ed254SubscriptionUseCase(SwimSecurityContext securityContext,
                                    SwimSubscriptionQueuePort queueOrchestrator,
                                    Ed254SubscriptionStore repository,
                                    Ed254SubscriptionHashPort hashCalculator,
                                    Ed254SubscriptionMappingPort subscriptionMapper,
                                    Ed254SubscriptionConfig config) {
        super(securityContext, queueOrchestrator, repository);
        this.hashCalculator = hashCalculator;
        this.subscriptionMapper = subscriptionMapper;
        this.config = config;
    }

    @Override
    protected String getQueuePrefix() {
        return QUEUE_PREFIX;
    }

    @Override
    protected Duration getDefaultTtl() {
        return config.defaultTtl();
    }

    @Override
    protected String getRequestedQueueName(Ed254SubscriptionCommand command) {
        return command.queueName();
    }

    @Override
    protected String calculateHash(Ed254SubscriptionCommand command, String userId) {
        return hashCalculator.calculateHash(command, userId);
    }

    @Override
    protected Ed254Subscription createEntity(Ed254SubscriptionCommand command, String userId, String queueName, String hash) {
        Ed254Subscription entity = subscriptionMapper.toEntity(command, userId, queueName, config.defaultTtl());
        entity.setSubscriptionHash(hash);
        return entity;
    }

    @Override
    protected Ed254SubscriptionResult mapToResponse(Ed254Subscription entity) {
        return subscriptionMapper.toResult(entity, config.providerName());
    }

    @Override
    protected void validateRequest(Ed254SubscriptionCommand command, String userId) {
        // ED-254 subscription validation is handled at the REST layer via Bean Validation.
        // No additional domain-level validation is required at this point.
    }
}
