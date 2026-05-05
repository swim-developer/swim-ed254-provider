package com.github.swim_developer.ed254.provider.infrastructure.out.persistence.entity;

import com.github.swim_developer.framework.domain.model.EventStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "ed254_events", indexes = {
        @Index(name = "idx_ed254_aerodrome", columnList = "aerodromeDesignator"),
        @Index(name = "idx_ed254_message_type", columnList = "messageType"),
        @Index(name = "idx_ed254_received_at", columnList = "receivedAt"),
        @Index(name = "idx_ed254_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ed254EventJpaEntity {

    @Id
    @Column(length = 100)
    private String eventId;

    @Column(name = "aerodrome_designator", length = 10)
    private String aerodromeDesignator;

    @Column(name = "message_type", length = 50)
    private String messageType;

    @Column(name = "publication_time")
    private Instant publicationTime;

    @Column(name = "creation_time")
    private Instant creationTime;

    @Column(name = "raw_payload", columnDefinition = "TEXT", nullable = false)
    private String rawPayload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    @Builder.Default
    private EventStatus status = EventStatus.RECEIVED;

    @Column(name = "received_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant receivedAt = Instant.now();

    private Instant processedAt;

    @Builder.Default
    private int deliveredCount = 0;

    @Column(name = "retry_count")
    @Builder.Default
    private int retryCount = 0;

    @Column(name = "first_message_after_service_outage")
    private boolean firstMessageAfterServiceOutage;
}
