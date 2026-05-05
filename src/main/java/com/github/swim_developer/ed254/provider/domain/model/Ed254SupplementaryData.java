package com.github.swim_developer.ed254.provider.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ed254SupplementaryData {

    private boolean delay;
    private boolean landingSequencePosition;
    private boolean amanStrategy;
    private boolean departureAerodrome;
    private boolean proposedProcedure;

    public boolean anyRequested() {
        return delay || landingSequencePosition || amanStrategy || departureAerodrome || proposedProcedure;
    }
}
