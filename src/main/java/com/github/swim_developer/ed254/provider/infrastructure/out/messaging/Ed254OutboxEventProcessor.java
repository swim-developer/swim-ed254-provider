package com.github.swim_developer.ed254.provider.infrastructure.out.messaging;

import com.github.swim_developer.ed254.provider.domain.model.Ed254EventEntity;
import com.github.swim_developer.ed254.provider.infrastructure.out.persistence.Ed254EventRepository;
import com.github.swim_developer.framework.domain.model.DeliveryResult;
import com.github.swim_developer.framework.infrastructure.out.cache.HandoffCache;
import com.github.swim_developer.framework.provider.application.messaging.AbstractOutboxEventProcessor;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.Timeout;

import java.time.temporal.ChronoUnit;
import com.github.swim_developer.ed254.provider.application.usecase.Ed254EventDeliveryUseCase;
import com.github.swim_developer.ed254.provider.infrastructure.out.messaging.Ed254OutboxEventProcessor;

@ApplicationScoped
@Slf4j
public class Ed254OutboxEventProcessor extends AbstractOutboxEventProcessor<Ed254EventEntity> {

    public static final String OUTBOX_EVENT_ADDRESS = "ed254.outbox.deliver";

    private final Ed254EventRepository eventRepository;
    private final Ed254EventDeliveryUseCase deliveryService;

    protected Ed254OutboxEventProcessor() {
        this(null, null, null, null);
    }

    @Inject
    protected Ed254OutboxEventProcessor(HandoffCache handoffCache,
                                         MeterRegistry registry,
                                         Ed254EventRepository eventRepository,
                                         Ed254EventDeliveryUseCase deliveryService) {
        super(handoffCache, registry);
        this.eventRepository = eventRepository;
        this.deliveryService = deliveryService;
    }

    @ConsumeEvent(OUTBOX_EVENT_ADDRESS)
    @Blocking
    @Timeout(value = 30, unit = ChronoUnit.SECONDS)
    @Bulkhead(250)
    @WithSpan("ed254.provider.outbox.deliver")
    public void onOutboxEvent(String eventId) {
        processWithMetrics(eventId);
    }

    @Override
    protected DeliveryResult deliver(Ed254EventEntity entity) {
        Span.current().setAttribute("ed254.messageType",
                entity.getMessageType() != null ? entity.getMessageType() : "unknown");
        Span.current().setAttribute("ed254.aerodrome",
                entity.getAerodromeDesignator() != null ? entity.getAerodromeDesignator() : "N/A");

        DeliveryResult result = deliveryService.deliverToMatchingSubscriptions(entity);

        Span.current().setAttribute("ed254.delivery.delivered", result.delivered());
        Span.current().setAttribute("ed254.delivery.failed", result.failed());

        return result;
    }

    @Override
    protected Ed254EventEntity findEntityById(String eventId) {
        return eventRepository.findDomainById(eventId);
    }

    @Override
    protected Ed254EventEntity mergeEntity(Ed254EventEntity detached) {
        return eventRepository.mergeDomainEntity(detached);
    }

    @Override
    protected Class<Ed254EventEntity> getEntityClass() {
        return Ed254EventEntity.class;
    }

    @Override
    protected String getMetricPrefix() {
        return "ed254";
    }
}
