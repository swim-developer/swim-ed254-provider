package com.github.swim_developer.ed254.provider.infrastructure.in.rest.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record UnsubscriptionResponse(
        UnsubscriptionStatus unsubscriptionResult,
        UnsubscriptionErrorReason errorReason) {
}
