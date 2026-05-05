package com.github.swim_developer.ed254.provider.infrastructure.in.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ed254SubscriptionResponse {

    private UUID subscriptionId;
    private String subscriptionResult;
    private String errorReason;
    private String queueName;
    private String subscriptionStatus;
    private Instant subscriptionEnd;
    private String providerName;
    private String heartbeatQueue;
}
