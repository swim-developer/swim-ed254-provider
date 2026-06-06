package com.github.swim_developer.ed254.provider.infrastructure.in.amqp;

import com.github.swim_developer.ed254.provider.domain.model.Ed254EventEntity;
import com.github.swim_developer.ed254.provider.domain.model.Ed254EventMetadata;
import com.github.swim_developer.ed254.provider.infrastructure.out.persistence.Ed254EventRepository;
import com.github.swim_developer.framework.domain.model.EventStatus;
import com.github.swim_developer.framework.provider.application.messaging.AfterCommitEventDispatcher;
import com.github.swim_developer.framework.application.port.in.SwimIngressHandler;
import com.github.swim_developer.framework.infrastructure.out.cache.HandoffCache;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.vertx.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.github.swim_developer.ed254.provider.infrastructure.out.xml.Ed254EventExtractor;
import com.github.swim_developer.ed254.provider.infrastructure.out.xml.Ed254PayloadValidator;
import com.github.swim_developer.ed254.provider.infrastructure.out.messaging.Ed254OutboxEventProcessor;

@ApplicationScoped
@Slf4j
public class Ed254IngressMessageHandler implements SwimIngressHandler {

    private static final String FAILED_STATUS = "failed";

    private final Ed254EventRepository eventRepository;
    private final Ed254EventExtractor eventExtractor;
    private final Ed254PayloadValidator payloadValidator;
    private final HandoffCache handoffCache;
    private final Vertx vertx;
    private final MeterRegistry registry;
    private final TransactionSynchronizationRegistry txSyncRegistry;

    @Inject
    public Ed254IngressMessageHandler(Ed254EventRepository eventRepository,
            Ed254EventExtractor eventExtractor,
            Ed254PayloadValidator payloadValidator,
            HandoffCache handoffCache,
            Vertx vertx,
            MeterRegistry registry,
            TransactionSynchronizationRegistry txSyncRegistry) {
        this.eventRepository = eventRepository;
        this.eventExtractor = eventExtractor;
        this.payloadValidator = payloadValidator;
        this.handoffCache = handoffCache;
        this.vertx = vertx;
        this.registry = registry;
        this.txSyncRegistry = txSyncRegistry;
    }

    @Override
    @Transactional
    @Retry(maxRetries = 2, delay = 500)
    @Timeout(value = 10, unit = ChronoUnit.SECONDS)
    @CircuitBreaker(requestVolumeThreshold = 10, failureRatio = 0.5, delay = 30000)
    @Bulkhead(value = 100)
    @WithSpan("ed254.provider.process")
    public void processEvent(String payload) {
        Timer.Sample timerSample = Timer.start(registry);
        log.debug("Processing ED-254 event — Phase 1: Validate & Persist");

        if (!validatePayload(payload)) {
            return;
        }

        List<Optional<Ed254EventMetadata>> extracted = eventExtractor.extract(payload);
        if (extracted.isEmpty() || extracted.getFirst().isEmpty()) {
            Span.current().setAttribute("ed254.extraction", FAILED_STATUS);
            log.warn("Failed to extract ED-254 event metadata");
            incrementFailedCounter("extraction_failed");
            return;
        }

        Ed254EventMetadata metadata = extracted.getFirst().get();
        String messageType = metadata.getMessageType() != null ? metadata.getMessageType() : "unknown";
        String aerodrome = metadata.getAerodromeDesignator() != null ? metadata.getAerodromeDesignator() : "N/A";
        incrementReceivedCounter(messageType);

        Span.current().setAttribute("ed254.messageType", messageType);
        Span.current().setAttribute("ed254.aerodrome", aerodrome);

        log.debug("Extracted event — Type: {}, Aerodrome: {}", messageType, aerodrome);

        Ed254EventEntity entity = persistWithStatusReceived(metadata, payload);
        if (entity == null) {
            Span.current().setAttribute("ed254.persist", FAILED_STATUS);
            return;
        }

        Span.current().setAttribute("ed254.persist", "success");
        dispatchForAsyncDelivery(entity);

        timerSample.stop(Timer.builder("ed254_event_processing_duration")
                .description("Time to process and persist an ED-254 event")
                .tag("type", messageType)
                .register(registry));

        log.info("Event persisted and dispatched — EventId: {}, Status: RECEIVED", entity.getEventId());
    }

    private Ed254EventEntity persistWithStatusReceived(Ed254EventMetadata metadata, String payload) {
        try {
            String eventId = generateEventId(metadata);
            Ed254EventEntity existing = eventRepository.findDomainById(eventId);

            if (existing != null) {
                log.debug("Event {} already exists, updating in place", eventId);
                existing.setAerodromeDesignator(metadata.getAerodromeDesignator());
                existing.setMessageType(metadata.getMessageType());
                existing.setPublicationTime(metadata.getPublicationTime());
                existing.setCreationTime(metadata.getCreationTime());
                existing.setRawPayload(payload);
                existing.setStatus(EventStatus.RECEIVED);
                existing.setDeliveredCount(0);
                existing.setRetryCount(0);
                existing.setProcessedAt(null);
                eventRepository.update(existing);
                incrementPersistedCounter();
                return existing;
            }

            Ed254EventEntity entity = Ed254EventEntity.builder()
                    .eventId(eventId)
                    .aerodromeDesignator(metadata.getAerodromeDesignator())
                    .messageType(metadata.getMessageType())
                    .publicationTime(metadata.getPublicationTime())
                    .creationTime(metadata.getCreationTime())
                    .rawPayload(payload)
                    .status(EventStatus.RECEIVED)
                    .build();
            eventRepository.persist(entity);
            incrementPersistedCounter();
            return entity;

        } catch (Exception e) {
            log.error("Failed to persist ED-254 event", e);
            incrementFailedCounter("persistence_failed");
            return null;
        }
    }

    private void dispatchForAsyncDelivery(Ed254EventEntity entity) {
        String eventId = entity.getEventId();
        txSyncRegistry.registerInterposedSynchronization(
                new AfterCommitEventDispatcher(eventId, entity, handoffCache, vertx,
                        Ed254OutboxEventProcessor.OUTBOX_EVENT_ADDRESS));
        log.debug("Event dispatch scheduled for after commit — EventId: {}", eventId);
    }

    private String generateEventId(Ed254EventMetadata metadata) {
        String aerodrome = metadata.getAerodromeDesignator() != null ? metadata.getAerodromeDesignator() : "UNK";
        String time = metadata.getCreationTime() != null
                ? String.valueOf(metadata.getCreationTime().toEpochMilli())
                : String.valueOf(Instant.now().toEpochMilli());
        return "ED254-" + aerodrome + "-" + time + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private boolean validatePayload(String payload) {
        var result = payloadValidator.validate(payload);
        if (!result.valid()) {
            Span.current().setAttribute("ed254.validation", FAILED_STATUS);
            log.warn("ED-254 XSD validation failed — event rejected: {}", result.errors());
            incrementFailedCounter("xsd_validation_failed");
            return false;
        }
        return true;
    }

    private void incrementReceivedCounter(String messageType) {
        Counter.builder("ed254_events_received_total")
                .description("Total ED-254 events received from Kafka")
                .tag("type", messageType)
                .register(registry)
                .increment();
    }

    private void incrementPersistedCounter() {
        Counter.builder("ed254_events_persisted_total")
                .description("Total ED-254 events persisted to database with RECEIVED status")
                .register(registry)
                .increment();
    }

    private void incrementFailedCounter(String reason) {
        Counter.builder("ed254_events_failed_total")
                .description("Total ED-254 events that failed processing")
                .tag("reason", reason)
                .register(registry)
                .increment();
    }
}
