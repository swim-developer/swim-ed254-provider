package com.github.swim_developer.ed254.provider.infrastructure.out.config;

import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@OpenAPIDefinition(
        info = @Info(
                title = "ED-254 Arrival Sequence Subscription Service",
                version = "1.0.0",
                description = """
                        ## Service Abstract
                        
                        
                        The Arrival Sequence Subscription Service allows service consumers to receive arrival sequence
                        information in accordance with the EUROCAE ED-254 specification. The service provides real-time
                        Extended AMAN (Arrival Manager) data including arrival sequences, estimated landing times,
                        constraint information, and provider exception notifications.
                        
                        
                        ## Operational Context
                        
                        
                        Extended Arrival Management (E-AMAN) enables arrival sequencing beyond the terminal area,
                        extending upstream to en-route sectors. This allows for earlier speed advisories, reducing
                        fuel burn, noise, and holding patterns while improving arrival predictability.
                        
                        
                        The service is part of SESAR Common Project 1 (CP1) deployment under EU Implementing
                        Regulation 2021/116, requiring ANSPs to consume Extended AMAN information for airports
                        within the AMAN horizon.
                        
                        
                        ## Service Capabilities
                        
                        
                        - **Subscribe to arrival sequences** — Receive notifications when sequence data updates
                        
                        - **Aerodrome filtering** — Filter by specific aerodromes of interest
                        
                        - **Message type selection** — Include or exclude supplementary data
                        
                        - **Distribution** — Automatic delivery via AMQP 1.0 when new data is available
                        
                        - **Subscription management** — Create, pause, resume, renew, unsubscribe
                        
                        - **Topic discovery** — Browse available arrival sequence services
                        
                        - **Problem reporting** — Consumers can report issues back to the provider
                        
                        - **Heartbeat monitoring** — Per-subscription health monitoring
                        
                        
                        ## Message Types
                        
                        
                        - **ArrivalSequenceData** — Ordered sequence of arriving flights with ETAs, constraints, runway
                        
                        - **AMANProviderException** — Service degradation, outage, or recovery notifications
                        
                        - **SubscriptionTechnicalMessage** — SUSPENDED, TERMINATED lifecycle events
                        
                        - **Heartbeat** — Periodic liveness signals per subscription
                        
                        
                        ## Intended Consumers
                        
                        
                        - Civil Air Navigation Service Providers (ANSPs)
                        
                        - Approach and En-Route Air Traffic Control Units (ATCUs)
                        
                        - Airport Operators
                        
                        - Network Manager
                        
                        
                        ## Compliance and Standards
                        
                        
                        This service is compliant with:
                        
                        - **EUROCAE ED-254** — Arrival Sequence Service Performance Standard (June 2018)
                        
                        - **EUROCONTROL SPEC-170** — SWIM-TI Yellow Profile protocol requirements
                        
                        - **EU Implementing Regulation 2021/116** — Common Project One (CP1)
                        
                        - **EASA DS.GE (2013/1768)** — Extended AMAN equipment requirements
                        
                        
                        ## Service Interfaces
                        
                        
                        1. **Subscription Interface** (WS-Light/REST) — Manage subscriptions via REST API (REQ-017254)
                        
                        2. **Distribution Interface** (AMQP 1.0) — Receive real-time arrival sequence data (REQ-0200)
                        
                        3. **CommunicateProblems** (REST) — Report issues to the provider
                        """,
                contact = @Contact(
                        name = "SWIM Developer",
                        url = "https://eur-registry.swim.aero",
                        email = "swim@eurocontrol.int"
                )
        ),
        tags = {
                @Tag(name = "ED-254 Subscriptions", description = "Arrival sequence subscription lifecycle management"),
                @Tag(name = "Topics", description = "Available arrival sequence services"),
                @Tag(name = "CommunicateProblems", description = "Consumer problem reporting")
        }
)
@SecurityScheme(
        securitySchemeName = "mTLS",
        type = SecuritySchemeType.MUTUALTLS,
        description = "Mutual TLS authentication using EACP (European Aviation Common PKI) certificates. All API calls require a valid X.509 client certificate issued by the EACP."
)
public class Ed254OpenApiConfiguration extends Application {
}
