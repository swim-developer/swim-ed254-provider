package com.github.swim_developer.ed254.provider.integration;

import com.github.swim_developer.ed254.provider.infrastructure.out.persistence.Ed254EventRepository;
import com.github.swim_developer.ed254.provider.infrastructure.out.persistence.Ed254SubscriptionRepository;
import com.github.swim_developer.ed254.provider.infrastructure.in.rest.dto.UnsubscriptionStatus;
import com.github.swim_developer.ed254.provider.domain.model.Ed254EventEntity;
import com.github.swim_developer.framework.domain.model.EventStatus;
import com.github.swim_developer.framework.domain.model.QualityOfService;
import com.github.swim_developer.framework.provider.infrastructure.out.security.JwtRoleValidator;
import com.github.swim_developer.framework.application.port.out.QueueProvisioningStrategy;
import io.quarkus.test.InjectMock;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.UserTransaction;
import org.junit.jupiter.api.BeforeEach;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

abstract class Ed254ProviderBaseIT {

    static final String SUBSCRIPTIONS_PATH = "/arrivalSequenceInformation/v1/subscriptions";
    static final String TEST_USER = "it-test-user";

    @InjectMock
    JwtRoleValidator jwtRoleValidator;

    @InjectMock
    QueueProvisioningStrategy queueProvisioner;

    @Inject
    Ed254SubscriptionRepository subscriptionRepository;

    @Inject
    Ed254EventRepository eventRepository;

    @Inject
    UserTransaction tx;

    @BeforeEach
    @Transactional
    void setUp() {
        eventRepository.deleteAll();
        subscriptionRepository.deleteAll();
        reset(jwtRoleValidator, queueProvisioner);
        when(jwtRoleValidator.getUsername()).thenReturn(TEST_USER);
        doNothing().when(jwtRoleValidator).validateAmqRole(anyString());
        doNothing().when(queueProvisioner).createQueue(anyString());
        doNothing().when(queueProvisioner).addSecurityRole(anyString(), anyString(), anyString());
        doNothing().when(queueProvisioner).removeQueue(anyString());
        doNothing().when(queueProvisioner).removeSecurityRole(anyString());
    }

    void seedInTx(String eventId, String aerodrome, Instant receivedAt) throws Exception {
        tx.begin();
        Ed254EventEntity event = Ed254EventEntity.builder()
                .eventId(eventId)
                .aerodromeDesignator(aerodrome)
                .messageType("ARRIVAL_SEQUENCE")
                .rawPayload("<payload>" + eventId + "</payload>")
                .status(EventStatus.RECEIVED)
                .receivedAt(receivedAt)
                .build();
        eventRepository.persist(event);
        tx.commit();
    }

    String createTestSubscription() {
        var body = Map.of(
                "subscriptionFilters", Map.of(
                        "destinationAerodrome", List.of(
                                Map.of("aerodromeDesignator", "EHAM-" + UUID.randomUUID().toString().substring(0, 4))
                        )
                ),
                "supplementaryData", Map.of(
                        "delay", false,
                        "landingSequencePosition", false,
                        "amanStrategy", false,
                        "departureAerodrome", false,
                        "proposedProcedure", false
                ),
                "qos", QualityOfService.AT_LEAST_ONCE.name()
        );

        return given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(SUBSCRIPTIONS_PATH)
                .then()
                .statusCode(201)
                .extract().jsonPath().getString("subscriptionId");
    }

    void deleteSubscription(String subscriptionId) {
        var response = given()
                .queryParam("subscriptionReference", subscriptionId)
                .when()
                .delete(SUBSCRIPTIONS_PATH)
                .then()
                .statusCode(200)
                .extract().jsonPath();

        assertThat(response.getString("unsubscriptionResult"))
                .isEqualTo(UnsubscriptionStatus.UNSUBSCRIPTION_SUCCESSFUL.name());
    }

    void resumeSubscription(String subscriptionId) {
        given()
                .when()
                .put(SUBSCRIPTIONS_PATH + "/{id}/resume", subscriptionId)
                .then()
                .statusCode(200);
    }
}
