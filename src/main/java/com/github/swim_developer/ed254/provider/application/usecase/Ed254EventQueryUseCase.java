package com.github.swim_developer.ed254.provider.application.usecase;

import com.github.swim_developer.ed254.provider.application.port.in.QueryEventPort;
import com.github.swim_developer.ed254.provider.domain.model.Ed254EventEntity;
import com.github.swim_developer.ed254.provider.domain.model.EventQueryFilters;
import com.github.swim_developer.ed254.provider.infrastructure.out.persistence.Ed254EventRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class Ed254EventQueryUseCase implements QueryEventPort {

    private final Ed254EventRepository eventRepository;
    private final MeterRegistry registry;

    @Inject
    public Ed254EventQueryUseCase(Ed254EventRepository eventRepository, MeterRegistry registry) {
        this.eventRepository = eventRepository;
        this.registry = registry;
    }

    @Override
    public String queryFeatures(EventQueryFilters filters) {
        Timer.Sample timerSample = Timer.start(registry);

        List<Ed254EventEntity> events = eventRepository.findWithFilters(filters);

        log.info("WFS GetFeature query returned {} events (startIndex={}, count={})",
                events.size(), filters.startIndex(), filters.count());

        String result = assembleResponse(events);

        timerSample.stop(Timer.builder("ed254_wfs_query_duration")
                .description("Time to execute WFS GetFeature query")
                .tag("resultCount", String.valueOf(events.size()))
                .register(registry));

        return result;
    }

    @Override
    public Optional<String> findByEventId(String eventId) {
        return eventRepository.findByEventId(eventId)
                .map(Ed254EventEntity::getRawPayload);
    }

    private String assembleResponse(List<Ed254EventEntity> events) {
        if (events.isEmpty()) {
            return """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <wfs:FeatureCollection xmlns:wfs="http://www.opengis.net/wfs/2.0"
                        numberMatched="0" numberReturned="0"/>
                    """;
        }

        String members = events.stream()
                .map(e -> "  <wfs:member>\n" + e.getRawPayload() + "\n  </wfs:member>")
                .collect(Collectors.joining("\n"));

        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <wfs:FeatureCollection xmlns:wfs="http://www.opengis.net/wfs/2.0"
                    numberMatched="%d" numberReturned="%d">
                %s
                </wfs:FeatureCollection>
                """.formatted(events.size(), events.size(), members);
    }
}
