package com.github.swim_developer.ed254.provider.infrastructure.in.rest;

import com.github.swim_developer.ed254.provider.application.port.in.QueryEventPort;
import com.github.swim_developer.ed254.provider.domain.model.EventQueryFilters;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
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

import java.time.Instant;
import java.time.format.DateTimeParseException;

@Path("/swim/v1/features")
@Produces(MediaType.APPLICATION_XML)
@SecurityRequirement(name = "mTLS")
@Tag(name = "Request Interface (WFS)",
        description = "OGC Web Feature Service 2.0 for ED-254 Arrival Sequence queries. " +
                "Implements wfs.getFeature operation per SWIM-TI Yellow Profile WS-Light binding.")
@Slf4j
public class FeatureResource {

    private final QueryEventPort queryService;

    @Inject
    public FeatureResource(QueryEventPort queryService) {
        this.queryService = queryService;
    }

    @GET
    @Operation(
            operationId = "getFeature",
            summary = "Request ED-254 Arrival Sequences (WFS GetFeature)",
            description = """
                    OGC Web Feature Service 2.0 interface for querying ED-254 Arrival Sequences.
                    Returns a WFS FeatureCollection wrapping the stored arrival sequence XML payloads.

                    **SWIM-TI Yellow Profile WS-Light Binding**
                    """
    )
    @APIResponse(responseCode = "200",
            description = "WFS FeatureCollection containing ED-254 arrival sequence events",
            content = @Content(mediaType = MediaType.APPLICATION_XML, schema = @Schema(implementation = String.class)))
    @APIResponse(responseCode = "400", description = "Invalid filter parameters")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response getFeature(
            @QueryParam("typeName")
            @Parameter(description = "Feature type to query", example = "arrivalSequence:ArrivalSequence")
            String typeName,

            @QueryParam("aerodromeDesignator")
            @Parameter(description = "Filter by ICAO aerodrome code", example = "LPPT")
            String aerodromeDesignator,

            @QueryParam("messageType")
            @Parameter(description = "Filter by message type", example = "ARRIVAL_SEQUENCE")
            String messageType,

            @QueryParam("validTime")
            @Parameter(description = "Validity time filter (ISO 8601)", example = "2026-01-15T00:00:00Z")
            String validTime,

            @QueryParam("startTime")
            @Parameter(description = "Filter events starting from this time (ISO 8601)", example = "2026-01-01T00:00:00Z")
            String startTime,

            @QueryParam("endTime")
            @Parameter(description = "Filter events ending before this time (ISO 8601)", example = "2026-01-31T23:59:59Z")
            String endTime,

            @QueryParam("startIndex")
            @Parameter(description = "WFS 2.0 pagination: zero-based index of first result", example = "0")
            Integer startIndex,

            @QueryParam("count")
            @Parameter(description = "WFS 2.0 pagination: maximum number of features to return", example = "100")
            Integer count) {

        log.info("WFS GetFeature request - typeName={}, aerodrome={}, messageType={}, validTime={}",
                typeName, aerodromeDesignator, messageType, validTime);

        Instant resolvedStart = parseInstant(startTime);
        Instant resolvedEnd = parseInstant(endTime);

        if (validTime != null && !validTime.isBlank()) {
            Instant validTimeInstant = parseInstant(validTime);
            if (validTimeInstant != null) {
                resolvedStart = validTimeInstant;
                resolvedEnd = validTimeInstant;
            }
        }

        int resolvedStartIndex = startIndex != null && startIndex >= 0 ? startIndex : 0;
        int resolvedCount = count != null && count > 0 ? count : 100;

        try {
            EventQueryFilters filters = new EventQueryFilters(
                    aerodromeDesignator, messageType,
                    resolvedStart, resolvedEnd,
                    resolvedStartIndex, resolvedCount);

            String result = queryService.queryFeatures(filters);

            return Response.ok(result)
                    .header("Content-Type", "application/xml; charset=UTF-8")
                    .build();

        } catch (Exception e) {
            log.error("Error executing WFS GetFeature query", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(buildErrorXml("NoApplicableCode", "Query execution failed: " + e.getMessage()))
                    .build();
        }
    }

    private Instant parseInstant(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            log.warn("Invalid date format: {}", value);
            return null;
        }
    }

    private String buildErrorXml(String code, String message) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <ows:ExceptionReport xmlns:ows="http://www.opengis.net/ows/2.0" version="2.0.0">
                    <ows:Exception exceptionCode="%s">
                        <ows:ExceptionText>%s</ows:ExceptionText>
                    </ows:Exception>
                </ows:ExceptionReport>
                """.formatted(code, message);
    }
}
