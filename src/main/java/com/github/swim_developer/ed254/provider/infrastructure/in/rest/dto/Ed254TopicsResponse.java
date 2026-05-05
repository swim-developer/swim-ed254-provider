package com.github.swim_developer.ed254.provider.infrastructure.in.rest.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

@RegisterForReflection
public record Ed254TopicsResponse(
        List<String> topics
) {
}
