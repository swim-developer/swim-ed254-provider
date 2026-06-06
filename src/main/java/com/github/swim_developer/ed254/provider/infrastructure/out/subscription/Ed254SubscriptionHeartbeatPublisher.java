package com.github.swim_developer.ed254.provider.infrastructure.out.subscription;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.swim_developer.framework.domain.model.QualityOfService;
import com.github.swim_developer.framework.domain.model.SubscriptionHeartbeat;
import com.github.swim_developer.framework.application.port.out.SubscriptionHeartbeatPublisher;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import com.github.swim_developer.ed254.provider.infrastructure.out.amqp.Ed254AmqpPublisher;

@Slf4j
@ApplicationScoped
public class Ed254SubscriptionHeartbeatPublisher implements SubscriptionHeartbeatPublisher {

    private final Ed254AmqpPublisher amqpPublisher;
    private final ObjectMapper objectMapper;

    @Inject
    public Ed254SubscriptionHeartbeatPublisher(Ed254AmqpPublisher amqpPublisher, ObjectMapper objectMapper) {
        this.amqpPublisher = amqpPublisher;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publishHeartbeat(String queueName, SubscriptionHeartbeat payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            amqpPublisher.publish(queueName, json, QualityOfService.AT_MOST_ONCE, HEARTBEAT_CONTENT_TYPE);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to publish heartbeat for queue: " + queueName, e);
        }
    }
}
