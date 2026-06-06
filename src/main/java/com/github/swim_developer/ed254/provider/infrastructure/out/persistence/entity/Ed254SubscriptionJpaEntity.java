package com.github.swim_developer.ed254.provider.infrastructure.out.persistence.entity;

import com.github.swim_developer.framework.domain.model.QualityOfService;
import com.github.swim_developer.framework.domain.model.SubscriptionStatus;
import com.github.swim_developer.framework.provider.infrastructure.out.converter.StringListConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ed254_subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ed254SubscriptionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "subscription_id")
    private UUID subscriptionId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "queue_name", nullable = false, unique = true)
    private String queue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QualityOfService qos;

    @Convert(converter = StringListConverter.class)
    @Column(name = "aerodromes", columnDefinition = "TEXT")
    private List<String> aerodromes;

    @Convert(converter = StringListConverter.class)
    @Column(name = "point_names", columnDefinition = "TEXT")
    private List<String> pointNames;

    @Convert(converter = StringListConverter.class)
    @Column(name = "runway_directions", columnDefinition = "TEXT")
    private List<String> runwayDirections;

    @Column(name = "sup_delay")
    @Builder.Default
    private boolean supDelay = false;

    @Column(name = "sup_landing_sequence_position")
    @Builder.Default
    private boolean supLandingSequencePosition = false;

    @Column(name = "sup_aman_strategy")
    @Builder.Default
    private boolean supAmanStrategy = false;

    @Column(name = "sup_departure_aerodrome")
    @Builder.Default
    private boolean supDepartureAerodrome = false;

    @Column(name = "sup_proposed_procedure")
    @Builder.Default
    private boolean supProposedProcedure = false;

    @Column(name = "flight_selectors_json", columnDefinition = "TEXT")
    private String flightSelectorsJson;

    @Column(name = "subscription_hash", unique = true)
    private String subscriptionHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "subscription_end", nullable = false)
    private Instant subscriptionEnd;
}
