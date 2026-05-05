package com.github.swim_developer.ed254.provider.domain.model;

import com.github.swim_developer.framework.domain.model.QualityOfService;

import java.util.Collections;
import java.util.List;

public record Ed254SubscriptionCommand(
        Ed254SubscriptionFilters subscriptionFilters,
        Ed254SupplementaryData supplementaryData,
        QualityOfService qos,
        String queueName) {

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
