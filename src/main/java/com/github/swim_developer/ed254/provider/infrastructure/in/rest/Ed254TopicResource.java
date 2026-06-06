package com.github.swim_developer.ed254.provider.infrastructure.in.rest;

import com.github.swim_developer.ed254.provider.infrastructure.in.rest.dto.Ed254TopicsResponse;
import com.github.swim_developer.framework.provider.application.subscription.TopicService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.Map;

@Path("/arrivalSequenceInformation/v1/topics")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@SecurityRequirement(name = "mTLS")
@Tag(name = "Topics", description = "Available arrival sequence services for subscription")
@Slf4j
public class Ed254TopicResource {

    private final TopicService topicService;

    @Inject
    public Ed254TopicResource(TopicService topicService) {
        this.topicService = topicService;
    }

    @GET
    @Operation(
            operationId = "getEd254Topics",
            summary = "Get list of available arrival sequence topics",
            description = "Returns the list of topics (service names) available for subscription as per ED-254 specification."
    )
    @APIResponse(
            responseCode = "200",
            description = "List of available topics",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = Ed254TopicsResponse.class)
            )
    )
    @APIResponse(responseCode = "401", description = "Authentication failed")
    public Response getTopics() {
        log.debug("Retrieving all ED-254 topics");
        Ed254TopicsResponse response = new Ed254TopicsResponse(topicService.getAllTopics());
        return Response.ok(response).build();
    }

    @GET
    @Path("/{topicId}")
    @Operation(
            operationId = "getEd254Topic",
            summary = "Get arrival sequence topic",
            description = "Returns confirmation that the topic exists and is available for subscription."
    )
    @APIResponse(
            responseCode = "200",
            description = "Topic exists",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = String.class)
            )
    )
    @APIResponse(responseCode = "404", description = "Topic not found")
    @APIResponse(responseCode = "401", description = "Authentication failed")
    public Response getTopic(
            @PathParam("topicId")
            @Parameter(
                    description = "Topic identifier (service name)",
                    required = true,
                    example = "ArrivalSequenceService"
            ) String topicId
    ) {
        log.debug("Retrieving ED-254 topic: {}", topicId);
        String topic = topicService.getTopic(topicId);
        return Response.ok(Map.of("topic", topic)).build();
    }
}
