package com.github.swim_developer.ed254.provider.infrastructure.out.amqp;

import com.github.swim_developer.framework.domain.model.QualityOfService;
import com.github.swim_developer.framework.application.port.out.AmqpPublisherHealthProvider;
import com.github.swim_developer.framework.provider.infrastructure.out.amqp.AbstractAmqpPublisher;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import java.time.temporal.ChronoUnit;
import java.util.UUID;

@ApplicationScoped
public class Ed254AmqpPublisher extends AbstractAmqpPublisher implements AmqpPublisherHealthProvider {

    private final Emitter<String> emitter;

    @Inject
    public Ed254AmqpPublisher(@Channel("ed254-subscriptions-out") Emitter<String> emitter) {
        this.emitter = emitter;
    }

    @Override
    protected Emitter<String> getEmitter() {
        return emitter;
    }

    @Override
    @Retry(maxRetries = 3, delay = 500, jitter = 200)
    @Timeout(value = 5, unit = ChronoUnit.SECONDS)
    @CircuitBreaker(requestVolumeThreshold = 10, failureRatio = 0.5, delay = 30000, successThreshold = 3)
    public void publishToQueue(String queue, String payload, QualityOfService qos, UUID subscriptionId) {
        super.publishToQueue(queue, payload, qos, subscriptionId);
    }
}
