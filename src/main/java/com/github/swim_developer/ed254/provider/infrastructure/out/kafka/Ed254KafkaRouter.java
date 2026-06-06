package com.github.swim_developer.ed254.provider.infrastructure.out.kafka;

import com.github.swim_developer.framework.application.port.out.SwimOutboxRouter;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;
import com.github.swim_developer.ed254.provider.infrastructure.out.kafka.Ed254KafkaRouter;

@Slf4j
@ApplicationScoped
public class Ed254KafkaRouter implements SwimOutboxRouter {

    @Channel("ed254-outbox")
    Emitter<String> outboxEmitter;

    @Channel("ed254-dlq")
    Emitter<String> dlqEmitter;

    @Override
    public void route(String messageId, String payload) {
        log.debug("Routing ED-254 event {} to Kafka outbox", messageId);
        outboxEmitter.send(Message.of(payload, Metadata.empty()));
    }

    @Override
    public void sendToDeadLetterQueue(String messageId, String payload) {
        log.warn("Sending ED-254 event {} to DLQ", messageId);
        dlqEmitter.send(Message.of(payload, Metadata.empty()));
    }
}
