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
public class Ed254DestinationAerodrome {

    private String aerodromeDesignator;
    private List<String> assignedArrivalRunway;
}
