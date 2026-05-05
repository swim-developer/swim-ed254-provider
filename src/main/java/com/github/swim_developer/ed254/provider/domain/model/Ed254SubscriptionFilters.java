package com.github.swim_developer.ed254.provider.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ed254SubscriptionFilters {

    private List<Ed254DestinationAerodrome> destinationAerodrome;
    private List<String> pointName;
    private List<Ed254FlightSelector> flightSelector;
}
