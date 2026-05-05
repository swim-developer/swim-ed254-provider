package com.github.swim_developer.ed254.provider.infrastructure.out.config;

import io.quarkus.smallrye.openapi.OpenApiFilter;
import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OpenApiFilter(stages = OpenApiFilter.RunStage.BUILD)
@Slf4j
public class Ed254OpenApiFilter implements OASFilter {

    @Override
    public void filterOpenAPI(OpenAPI openAPI) {

        openAPI.addExtension("x-service-identification", Map.of(
                "edition", "01.00.00",
                "referenceDate", "2018-06-01",
                "serviceType", "SWIM_DEFINITION",
                "businessActivityType", "ARRIVAL_MANAGEMENT",
                "informationCategory", "ARRIVAL_SEQUENCE_INFORMATION",
                "specification", "EUROCAE ED-254"
        ));

        openAPI.addExtension("x-glossary", getGlossary());

        openAPI.addExtension("x-filtering-capabilities", Map.of(
                "subscription-interface", Map.of(
                        "description", "The Subscription Interface allows selection of aerodromes of interest and message type preferences.",
                        "filters", List.of("aerodromeDesignator", "pointNames", "runwayDirections", "includeSupplementaryData")
                )
        ));

        openAPI.addExtension("x-data-quality", Map.of(
                "regulatory-compliance", List.of(
                        "EUROCAE ED-254 (June 2018)",
                        "EU Implementing Regulation 2021/116 (Common Project One)",
                        "EASA DS.GE (2013/1768)"
                ),
                "encoding", List.of("ED-254 Arrival Sequence XML Schema"),
                "message-types", List.of("ArrivalSequenceData", "AMANProviderException")
        ));

        openAPI.addExtension("x-message-exchange-patterns", Map.of(
                "publish-subscribe", Map.of(
                        "description", "A REST implementation handles the subscription (Subscription Interface). An AMQP 1.0 implementation handles message distribution (Distribution Interface).",
                        "protocol", "AMQP 1.0",
                        "binding", "SWIM-TI Yellow Profile AMQP Messaging (REQ-0200)"
                ),
                "subscription-management", Map.of(
                        "protocol", "HTTP/REST",
                        "binding", "SWIM-TI Yellow Profile WS-Light (REQ-017254)"
                )
        ));

        openAPI.addExtension("x-service-behaviour", Map.of(
                "subscription-lifecycle", List.of(
                        "Create subscription (status: PAUSED by default, REQ 0100)",
                        "Activate subscription (status: ACTIVE, REQ 0110)",
                        "Pause subscription (status: PAUSED, REQ 0120)",
                        "Resume subscription (status: ACTIVE, REQ 0130)",
                        "Renew subscription (extends subscriptionEnd, REQ 0140)",
                        "Unsubscribe (permanent removal, REQ 0150/0155)"
                ),
                "distribution-flow", List.of(
                        "Arrival sequence computed by AMAN",
                        "System generates ED-254 XML message",
                        "Message distributed to AMQP queues based on subscription filters",
                        "Consumer connects to queue and consumes message",
                        "Consumer validates sequence continuity and freshness"
                ),
                "problem-reporting", List.of(
                        "Consumer detects issue with received data",
                        "Consumer sends CommunicateProblems request (REQ 0160)",
                        "Provider logs and processes the problem report"
                )
        ));

        openAPI.addExtension("x-scope", Map.of(
                "geographic", "Extended AMAN horizon (typically 150-350 NM from destination)",
                "airports", "Airports with Extended AMAN operations under CP1",
                "update-frequency", "Typically every 10-30 seconds per aerodrome"
        ));

        openAPI.setExternalDocs(OASFactory.createExternalDocumentation()
                .description("EUROCAE ED-254 — Arrival Sequence Service Performance Standard")
                .url("https://www.eurocae.net/products/ed-254/"));
    }

    private static Map<String, String> getGlossary() {
        Map<String, String> glossary = new HashMap<>();
        glossary.put("AMAN", "Arrival Manager");
        glossary.put("E-AMAN", "Extended Arrival Manager");
        glossary.put("AMQP", "Advanced Message Queuing Protocol");
        glossary.put("ANSP", "Air Navigation Service Provider");
        glossary.put("ATCU", "Air Traffic Control Unit");
        glossary.put("ATM", "Air Traffic Management");
        glossary.put("CP1", "Common Project One");
        glossary.put("EACP", "European Aviation Common PKI");
        glossary.put("ED-254", "EUROCAE Arrival Sequence Service Performance Standard");
        glossary.put("ETA", "Estimated Time of Arrival");
        glossary.put("ETOT", "Estimated Take-Off Time");
        glossary.put("PKI", "Public Key Infrastructure");
        glossary.put("SASL", "Simple Authentication and Security Layer");
        glossary.put("SESAR", "Single European Sky ATM Research");
        glossary.put("SWIM", "System Wide Information Management");
        glossary.put("TI", "Technical Infrastructure");
        glossary.put("TLS", "Transport Layer Security");
        glossary.put("TTL", "Time To Live");
        return glossary;
    }
}
