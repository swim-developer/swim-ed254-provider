package com.github.swim_developer.ed254.provider.infrastructure.in.rest.dto;

import com.github.swim_developer.ed254.provider.domain.model.Ed254DestinationAerodrome;
import com.github.swim_developer.ed254.provider.domain.model.Ed254SubscriptionFilters;
import com.github.swim_developer.ed254.provider.domain.model.Ed254SupplementaryData;
import com.github.swim_developer.framework.domain.model.QualityOfService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ed254SubscriptionRequest {

    private Ed254SubscriptionFilters subscriptionFilters;
    private Ed254SupplementaryData supplementaryData;
    private QualityOfService qos;
    private String queueName;

    public List<String> extractAerodromeDesignators() {
        if (subscriptionFilters == null || subscriptionFilters.getDestinationAerodrome() == null) {
            return Collections.emptyList();
        }
        return subscriptionFilters.getDestinationAerodrome().stream()
                .map(Ed254DestinationAerodrome::getAerodromeDesignator)
                .toList();
    }

    public List<String> extractPointNames() {
        if (subscriptionFilters == null || subscriptionFilters.getPointName() == null) {
            return Collections.emptyList();
        }
        return subscriptionFilters.getPointName();
    }

    public List<String> extractRunwayDesignators() {
        if (subscriptionFilters == null || subscriptionFilters.getDestinationAerodrome() == null) {
            return Collections.emptyList();
        }
        return subscriptionFilters.getDestinationAerodrome().stream()
                .filter(d -> d.getAssignedArrivalRunway() != null)
                .flatMap(d -> d.getAssignedArrivalRunway().stream())
                .toList();
    }
}
