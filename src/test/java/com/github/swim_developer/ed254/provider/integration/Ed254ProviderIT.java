package com.github.swim_developer.ed254.provider.integration;

import com.github.swim_developer.ed254.provider.infrastructure.in.rest.dto.UnsubscriptionErrorReason;
import com.github.swim_developer.ed254.provider.infrastructure.in.rest.dto.UnsubscriptionStatus;
import com.github.swim_developer.framework.domain.model.QualityOfService;
import com.github.swim_developer.framework.domain.model.SubscriptionStatus;
import com.github.swim_developer.framework.infrastructure.testing.TestNameLoggerExtension;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestSecurity(user = "it-test-user", roles = "user")
@ExtendWith(TestNameLoggerExtension.class)
@Timeout(value = 2, unit = TimeUnit.MINUTES)
class Ed254ProviderIT extends Ed254ProviderBaseIT {

    @Test
    @Order(1)
    void createSubscriptionReturns201WithPausedStatus() {
        var body = Map.of(
                "subscriptionFilters", Map.of(
                        "destinationAerodrome", List.of(
                                Map.of("aerodromeDesignator", "ESSA"),
                                Map.of("aerodromeDesignator", "ENGM")
                        ),
                        "pointName", List.of("SUGOL")
                ),
                "supplementaryData", Map.of(
                        "delay", true,
                        "landingSequencePosition", false,
                        "amanStrategy", false,
                        "departureAerodrome", false,
                        "proposedProcedure", false
                ),
                "qos", QualityOfService.AT_LEAST_ONCE.name()
        );

        var response = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(SUBSCRIPTIONS_PATH)
                .then()
                .statusCode(201)
                .extract().jsonPath();

        assertThat(response.getString("subscriptionId")).isNotNull();
        assertThat(response.getString("queueName")).startsWith("ed254-");
        assertThat(response.getString("subscriptionStatus")).isEqualTo(SubscriptionStatus.PAUSED.name());
        assertThat(response.getString("subscriptionResult")).isEqualTo("SUBSCRIPTION_SUCCESSFUL");
        assertThat(response.getString("heartbeatQueue")).endsWith("-heartbeat");

        verify(queueProvisioner, atLeast(1)).createQueue(anyString());
        verify(queueProvisioner, atLeast(1)).addSecurityRole(anyString(), anyString(), anyString());
    }

    @Test
    @Order(2)
    void idempotencyReturnsSameSubscriptionForDuplicateRequest() {
        var body = Map.of(
                "subscriptionFilters", Map.of(
                        "destinationAerodrome", List.of(Map.of("aerodromeDesignator", "LFPG"))
                ),
                "supplementaryData", Map.of(
                        "delay", true,
                        "landingSequencePosition", false,
                        "amanStrategy", false,
                        "departureAerodrome", false,
                        "proposedProcedure", false
                ),
                "qos", QualityOfService.AT_LEAST_ONCE.name()
        );

        var first = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(SUBSCRIPTIONS_PATH)
                .then()
                .statusCode(201)
                .extract().jsonPath();

        var second = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(SUBSCRIPTIONS_PATH)
                .then()
                .statusCode(201)
                .extract().jsonPath();

        assertThat(first.getString("subscriptionId"))
                .isEqualTo(second.getString("subscriptionId"));
    }

    @Test
    @Order(3)
    void getSubscriptionReturnsDetails() {
        String subscriptionId = createTestSubscription();

        var response = given()
                .when()
                .get(SUBSCRIPTIONS_PATH + "/{id}", subscriptionId)
                .then()
                .statusCode(200)
                .extract().jsonPath();

        assertThat(response.getString("subscriptionId")).isEqualTo(subscriptionId);
        assertThat(response.getString("subscriptionStatus")).isEqualTo(SubscriptionStatus.PAUSED.name());
    }

    @Test
    @Order(4)
    void getSubscriptionReturns404ForUnknownId() {
        given()
                .when()
                .get(SUBSCRIPTIONS_PATH + "/{id}", UUID.randomUUID())
                .then()
                .statusCode(404);
    }

    @Test
    @Order(5)
    void listSubscriptionsReturnsUserSubscriptions() {
        createTestSubscription();

        var response = given()
                .when()
                .get(SUBSCRIPTIONS_PATH)
                .then()
                .statusCode(200)
                .extract().jsonPath();

        assertThat(response.getList("$")).isNotEmpty();
    }

    @Test
    @Order(6)
    void resumeSubscriptionActivates() {
        String subscriptionId = createTestSubscription();

        var response = given()
                .when()
                .put(SUBSCRIPTIONS_PATH + "/{id}/resume", subscriptionId)
                .then()
                .statusCode(200)
                .extract().jsonPath();

        assertThat(response.getString("subscriptionStatus")).isEqualTo(SubscriptionStatus.ACTIVE.name());

        var persisted = subscriptionRepository.findByIdOptional(UUID.fromString(subscriptionId));
        assertThat(persisted).isPresent();
        assertThat(persisted.get().getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    }

    @Test
    @Order(7)
    void pauseActiveSubscription() {
        String subscriptionId = createTestSubscription();
        resumeSubscription(subscriptionId);

        var response = given()
                .when()
                .put(SUBSCRIPTIONS_PATH + "/{id}/pause", subscriptionId)
                .then()
                .statusCode(200)
                .extract().jsonPath();

        assertThat(response.getString("subscriptionStatus")).isEqualTo(SubscriptionStatus.PAUSED.name());
    }

    @Test
    @Order(8)
    void deleteSubscriptionSoftDeletes() {
        String subscriptionId = createTestSubscription();

        var response = given()
                .queryParam("subscriptionReference", subscriptionId)
                .when()
                .delete(SUBSCRIPTIONS_PATH)
                .then()
                .statusCode(200)
                .extract().jsonPath();

        assertThat(response.getString("unsubscriptionResult"))
                .isEqualTo(UnsubscriptionStatus.UNSUBSCRIPTION_SUCCESSFUL.name());

        var persisted = subscriptionRepository.findByIdOptional(UUID.fromString(subscriptionId));
        assertThat(persisted).isPresent();
        assertThat(persisted.get().getStatus()).isEqualTo(SubscriptionStatus.DELETED);
    }

    @Test
    @Order(9)
    void deletedSubscriptionIsImmutable() {
        String subscriptionId = createTestSubscription();
        deleteSubscription(subscriptionId);

        given()
                .when()
                .put(SUBSCRIPTIONS_PATH + "/{id}/resume", subscriptionId)
                .then()
                .statusCode(400);
    }

    @Test
    @Order(10)
    void renewSubscriptionExtendsTtl() {
        String subscriptionId = createTestSubscription();

        var getResponse = given()
                .when()
                .get(SUBSCRIPTIONS_PATH + "/{id}", subscriptionId)
                .then()
                .statusCode(200)
                .extract().jsonPath();
        Instant before = Instant.parse(getResponse.getString("subscriptionEnd"));

        Instant start = Instant.now();
        await()
                .atMost(Duration.ofSeconds(2))
                .pollInterval(Duration.ofMillis(5))
                .until(() -> Duration.between(start, Instant.now()).toMillis() >= 50);

        var renewResponse = given()
                .when()
                .put(SUBSCRIPTIONS_PATH + "/{id}/renew", subscriptionId)
                .then()
                .statusCode(200)
                .extract().jsonPath();

        assertThat(renewResponse.getString("subscriptionId")).isEqualTo(subscriptionId);
        Instant after = Instant.parse(renewResponse.getString("subscriptionEnd"));
        assertThat(after).isAfter(before);
    }

    @Test
    @Order(11)
    void renewDeletedSubscriptionReturns400() {
        String subscriptionId = createTestSubscription();
        deleteSubscription(subscriptionId);

        given()
                .when()
                .put(SUBSCRIPTIONS_PATH + "/{id}/renew", subscriptionId)
                .then()
                .statusCode(400);
    }

    @Test
    @Order(12)
    void deleteNonExistentSubscriptionReturnsWrongReference() {
        var response = given()
                .queryParam("subscriptionReference", UUID.randomUUID())
                .when()
                .delete(SUBSCRIPTIONS_PATH)
                .then()
                .statusCode(200)
                .extract().jsonPath();

        assertThat(response.getString("unsubscriptionResult"))
                .isEqualTo(UnsubscriptionStatus.UNSUBSCRIPTION_FAILURE.name());
        assertThat(response.getString("errorReason"))
                .isEqualTo(UnsubscriptionErrorReason.WRONG_REFERENCE.name());
    }

    @Test
    @Order(13)
    void fullLifecycleCreateResumeDeletePauseRenew() {
        String subscriptionId = createTestSubscription();

        given().put(SUBSCRIPTIONS_PATH + "/{id}/resume", subscriptionId).then().statusCode(200);
        given().put(SUBSCRIPTIONS_PATH + "/{id}/pause", subscriptionId).then().statusCode(200);
        given().put(SUBSCRIPTIONS_PATH + "/{id}/renew", subscriptionId).then().statusCode(200);
        given().put(SUBSCRIPTIONS_PATH + "/{id}/resume", subscriptionId).then().statusCode(200);
        deleteSubscription(subscriptionId);

        var entity = subscriptionRepository.findByIdOptional(UUID.fromString(subscriptionId)).orElseThrow();
        assertThat(entity.getStatus()).isEqualTo(SubscriptionStatus.DELETED);
    }
}
