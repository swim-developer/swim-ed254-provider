package com.github.swim_developer.ed254.provider.infrastructure.in.rest;

import com.github.swim_developer.ed254.provider.infrastructure.in.rest.dto.Ed254SubscriptionRequest;
import com.github.swim_developer.ed254.provider.infrastructure.in.rest.dto.Ed254SubscriptionResponse;
import com.github.swim_developer.ed254.provider.application.port.in.ManageSubscriptionPort;
import com.github.swim_developer.ed254.provider.domain.model.Ed254SubscriptionCommand;
import com.github.swim_developer.ed254.provider.domain.model.Ed254SubscriptionResult;
import com.github.swim_developer.ed254.provider.infrastructure.in.rest.dto.UnsubscriptionErrorReason;
import com.github.swim_developer.ed254.provider.infrastructure.in.rest.dto.UnsubscriptionResponse;
import com.github.swim_developer.ed254.provider.infrastructure.in.rest.dto.UnsubscriptionStatus;
import com.github.swim_developer.framework.domain.model.SubscriptionStatus;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@Slf4j
@Path("/arrivalSequenceInformation/v1/subscriptions")
@Tag(name = "ED-254 Subscriptions", description = "Arrival Sequence subscription management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class Ed254SubscriptionResource {

    private final ManageSubscriptionPort subscriptionService;

    @Inject
    public Ed254SubscriptionResource(ManageSubscriptionPort subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @POST
    @Operation(summary = "Create a new arrival sequence subscription")
    public Response createSubscription(Ed254SubscriptionRequest request) {
        Ed254SubscriptionResponse response = toResponse(subscriptionService.createSubscription(toCommand(request)));
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Operation(summary = "List all subscriptions for current user")
    public List<Ed254SubscriptionResponse> listSubscriptions() {
        return subscriptionService.listSubscriptions().stream().map(this::toResponse).toList();
    }

    @GET
    @Path("/{subscriptionId}")
    @Operation(summary = "Get subscription details")
    public Ed254SubscriptionResponse getSubscription(@PathParam("subscriptionId") UUID subscriptionId) {
        return toResponse(subscriptionService.getSubscription(subscriptionId));
    }

    @PUT
    @Path("/{subscriptionId}/pause")
    @Operation(summary = "Pause subscription")
    public Ed254SubscriptionResponse pauseSubscription(@PathParam("subscriptionId") UUID subscriptionId) {
        return toResponse(subscriptionService.updateStatus(subscriptionId, SubscriptionStatus.PAUSED));
    }

    @PUT
    @Path("/{subscriptionId}/resume")
    @Operation(summary = "Resume subscription")
    public Ed254SubscriptionResponse resumeSubscription(@PathParam("subscriptionId") UUID subscriptionId) {
        return toResponse(subscriptionService.updateStatus(subscriptionId, SubscriptionStatus.ACTIVE));
    }

    @PUT
    @Path("/{subscriptionId}/renew")
    @Operation(summary = "Renew subscription by extending subscriptionEnd",
               description = "SWIM/ED-254 requirement: Consumer responsibility to renew before expiration. " +
                             "Extends subscriptionEnd by default TTL (24h) from now.")
    public Ed254SubscriptionResponse renewSubscription(@PathParam("subscriptionId") UUID subscriptionId) {
        return toResponse(subscriptionService.renewSubscription(subscriptionId, null));
    }

    @DELETE
    @Operation(summary = "Unsubscribe from arrival sequence information (ED-254 REQ 0150, REQ 0155)")
    public UnsubscriptionResponse deleteSubscription(
            @Parameter(description = "Subscription reference to unsubscribe", required = true)
            @QueryParam("subscriptionReference") UUID subscriptionReference) {
        if (subscriptionReference == null) {
            return new UnsubscriptionResponse(UnsubscriptionStatus.UNSUBSCRIPTION_FAILURE,
                    UnsubscriptionErrorReason.NOT_INTERPRETABLE);
        }
        try {
            subscriptionService.deleteSubscription(subscriptionReference);
            return new UnsubscriptionResponse(UnsubscriptionStatus.UNSUBSCRIPTION_SUCCESSFUL, null);
        } catch (jakarta.ws.rs.NotFoundException e) {
            return new UnsubscriptionResponse(UnsubscriptionStatus.UNSUBSCRIPTION_FAILURE,
                    UnsubscriptionErrorReason.WRONG_REFERENCE);
        }
    }

    private Ed254SubscriptionCommand toCommand(Ed254SubscriptionRequest request) {
        return new Ed254SubscriptionCommand(
                request.getSubscriptionFilters(),
                request.getSupplementaryData(),
                request.getQos(),
                request.getQueueName()
        );
    }

    private Ed254SubscriptionResponse toResponse(Ed254SubscriptionResult result) {
        return Ed254SubscriptionResponse.builder()
                .subscriptionId(result.subscriptionId())
                .subscriptionResult(result.subscriptionResult())
                .errorReason(result.errorReason())
                .queueName(result.queueName())
                .subscriptionStatus(result.subscriptionStatus())
                .subscriptionEnd(result.subscriptionEnd())
                .providerName(result.providerName())
                .heartbeatQueue(result.heartbeatQueue())
                .build();
    }
}
