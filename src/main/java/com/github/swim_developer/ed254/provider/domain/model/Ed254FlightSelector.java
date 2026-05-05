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
public class Ed254FlightSelector {

    private String arcid;
    private String ades;
    private String adep;
    private String eobt;
    private String eobd;
    private String ifplId;
}
