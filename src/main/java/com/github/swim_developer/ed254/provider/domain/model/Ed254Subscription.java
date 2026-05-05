package com.github.swim_developer.ed254.provider.domain.model;

import com.github.swim_developer.framework.domain.model.QualityOfService;
import com.github.swim_developer.framework.domain.model.SubscriptionStatus;
import com.github.swim_developer.framework.domain.model.SwimSubscription;
import com.github.swim_developer.framework.domain.model.SwimSubscriptionEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ed254Subscription implements SwimSubscription<Ed254EventEntity>, SwimSubscriptionEntity {

    private UUID subscriptionId;
    private String userId;
    private String queue;
    private SubscriptionStatus status;
    private QualityOfService qos;
    private List<String> aerodromes;
    private List<String> pointNames;
    private List<String> runwayDirections;

    @Builder.Default
    private boolean supDelay = false;

    @Builder.Default
    private boolean supLandingSequencePosition = false;

    @Builder.Default
    private boolean supAmanStrategy = false;

    @Builder.Default
    private boolean supDepartureAerodrome = false;

    @Builder.Default
    private boolean supProposedProcedure = false;

    private String flightSelectorsJson;
    private String subscriptionHash;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant subscriptionEnd;

    @Override
    public Predicate<Ed254EventEntity> toFilter() {
        return this::matchesAerodrome;
    }

    private boolean matchesAerodrome(Ed254EventEntity event) {
        if (aerodromes == null || aerodromes.isEmpty()) {
            return true;
        }
        return aerodromes.contains(event.getAerodromeDesignator());
    }

    public boolean anySupplementaryDataRequested() {
        return supDelay || supLandingSequencePosition || supAmanStrategy
                || supDepartureAerodrome || supProposedProcedure;
    }
}
