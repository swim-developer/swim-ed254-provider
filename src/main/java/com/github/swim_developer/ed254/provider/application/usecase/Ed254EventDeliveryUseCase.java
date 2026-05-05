package com.github.swim_developer.ed254.provider.application.usecase;

import com.github.swim_developer.ed254.provider.domain.model.Ed254EventEntity;
import com.github.swim_developer.ed254.provider.domain.model.Ed254Subscription;
import com.github.swim_developer.ed254.provider.domain.port.out.Ed254SubscriptionStore;
import com.github.swim_developer.ed254.provider.domain.model.Ed254FailedDeliveryEntity;
import com.github.swim_developer.framework.domain.model.QualityOfService;
import com.github.swim_developer.framework.domain.model.SubscriptionStatus;
import com.github.swim_developer.framework.application.port.out.SwimAmqpPublisherPort;
import com.github.swim_developer.framework.provider.application.subscription.AbstractEventDeliveryService;
import com.github.swim_developer.framework.application.port.out.FailedDeliveryStore;
import com.github.swim_developer.framework.domain.model.SwimFailedDelivery;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@ApplicationScoped
public class Ed254EventDeliveryUseCase extends AbstractEventDeliveryService<Ed254EventEntity, Ed254EventEntity, Ed254Subscription> {

    private final Ed254SubscriptionStore subscriptionRepository;
    private final SwimAmqpPublisherPort amqpPublisher;
    private final FailedDeliveryStore<Ed254FailedDeliveryEntity> failedDeliveryRepository;
    private final MeterRegistry registry;

    @Inject
    public Ed254EventDeliveryUseCase(Ed254SubscriptionStore subscriptionRepository,
            SwimAmqpPublisherPort amqpPublisher,
            FailedDeliveryStore<Ed254FailedDeliveryEntity> failedDeliveryRepository,
            MeterRegistry registry) {
        this.subscriptionRepository = subscriptionRepository;
        this.amqpPublisher = amqpPublisher;
        this.failedDeliveryRepository = failedDeliveryRepository;
        this.registry = registry;
    }

    @Override
    protected String extractEventId(Ed254EventEntity entity) {
        return entity.getEventId();
    }

    @Override
    protected Ed254EventEntity toFilterableModel(Ed254EventEntity entity) {
        return entity;
    }

    @Override
    protected String extractPayload(Ed254EventEntity entity) {
        return entity.getRawPayload();
    }

    @Override
    protected List<Ed254Subscription> loadActiveSubscriptions() {
        return subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE);
    }

    @Override
    protected void publishToQueue(String queue, String payload, QualityOfService qos, UUID subscriptionId) {
        amqpPublisher.publishToQueue(queue, payload, qos, subscriptionId);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Optional<FailedDeliveryStore<SwimFailedDelivery>> getFailedDeliveryStore() {
        return Optional.of((FailedDeliveryStore<SwimFailedDelivery>) (FailedDeliveryStore<?>) failedDeliveryRepository);
    }

    @Override
    protected void onDeliverySuccess(Ed254EventEntity entity, Ed254Subscription subscription) {
        Counter.builder("ed254_events_delivered_total")
                .description("Total ED-254 events delivered to AMQP queues")
                .register(registry)
                .increment();
    }

    @Override
    protected void onDeliveryFailure(Ed254EventEntity entity, Ed254Subscription subscription, Exception e) {
        Counter.builder("ed254_events_delivery_failed_total")
                .description("Total ED-254 events that failed delivery per subscriber")
                .register(registry)
                .increment();
    }
}
