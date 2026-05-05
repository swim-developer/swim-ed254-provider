package com.github.swim_developer.ed254.provider.infrastructure.in.rest;

import com.github.swim_developer.ed254.provider.application.usecase.Ed254ProblemReportUseCase;
import com.github.swim_developer.framework.domain.model.DataValidationResult;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Slf4j
@Path("/arrivalSequenceInformation/v1/problems")
@Tag(name = "ED-254 CommunicateProblems",
     description = "Enables consumers to report data quality issues in arrival sequence data (ED-254 REQ 0165, REQ 0170, REQ 0190)")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class Ed254ProblemsResource {

    private final Ed254ProblemReportUseCase problemReportService;

    @Inject
    public Ed254ProblemsResource(Ed254ProblemReportUseCase problemReportService) {
        this.problemReportService = problemReportService;
    }

    @POST
    @Operation(summary = "Report data quality problems in received arrival sequence data")
    @APIResponse(responseCode = "202", description = "Problem report accepted")
    @APIResponse(responseCode = "400", description = "Invalid request body")
    @APIResponse(responseCode = "401", description = "Unauthorized")
    @APIResponse(responseCode = "500", description = "Internal Server Error")
    @APIResponse(responseCode = "503", description = "Service Unavailable")
    public Response communicateProblems(DataValidationResult request, @Context SecurityContext securityContext) {
        String reportedBy = securityContext.getUserPrincipal() != null
                ? securityContext.getUserPrincipal().getName()
                : "anonymous";
        problemReportService.processProblemReport(request, reportedBy);
        return Response.accepted().build();
    }
}
