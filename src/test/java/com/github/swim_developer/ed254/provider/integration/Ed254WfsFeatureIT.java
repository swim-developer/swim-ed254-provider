package com.github.swim_developer.ed254.provider.integration;

import com.github.swim_developer.framework.infrastructure.testing.TestNameLoggerExtension;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestSecurity(user = "it-test-user", roles = "user")
@ExtendWith(TestNameLoggerExtension.class)
@Timeout(value = 2, unit = TimeUnit.MINUTES)
class Ed254WfsFeatureIT extends Ed254ProviderBaseIT {

    @Test
    @Order(1)
    void returnsEmptyCollectionWhenNoEvents() {
        String xml = given()
                .when()
                .get("/swim/v1/features")
                .then()
                .statusCode(200)
                .contentType("application/xml")
                .extract().asString();

        assertThat(xml).contains("numberMatched=\"0\"")
                .contains("numberReturned=\"0\"");
    }

    @Test
    @Order(2)
    void returnsPersistedEvents() throws Exception {
        seedInTx("EVT-001", "LPPT", Instant.now());
        seedInTx("EVT-002", "ESSA", Instant.now());

        String xml = given()
                .when()
                .get("/swim/v1/features")
                .then()
                .statusCode(200)
                .contentType("application/xml")
                .extract().asString();

        assertThat(xml).contains("numberMatched=\"2\"")
                .contains("numberReturned=\"2\"")
                .contains("<payload>EVT-001</payload>")
                .contains("<payload>EVT-002</payload>");
    }

    @Test
    @Order(3)
    void filtersByAerodrome() throws Exception {
        seedInTx("EVT-010", "LPPT", Instant.now());
        seedInTx("EVT-011", "ESSA", Instant.now());

        String xml = given()
                .queryParam("aerodromeDesignator", "LPPT")
                .when()
                .get("/swim/v1/features")
                .then()
                .statusCode(200)
                .extract().asString();

        assertThat(xml).contains("numberMatched=\"1\"")
                .contains("<payload>EVT-010</payload>")
                .doesNotContain("<payload>EVT-011</payload>");
    }

    @Test
    @Order(4)
    void filtersByValidTime() throws Exception {
        Instant now = Instant.now();
        seedInTx("EVT-020", "LPPT", now.minusSeconds(3600));
        seedInTx("EVT-021", "LPPT", now.plusSeconds(3600));

        String xml = given()
                .queryParam("startTime", now.toString())
                .when()
                .get("/swim/v1/features")
                .then()
                .statusCode(200)
                .extract().asString();

        assertThat(xml).contains("numberMatched=\"1\"")
                .contains("<payload>EVT-021</payload>")
                .doesNotContain("<payload>EVT-020</payload>");
    }

    @Test
    @Order(5)
    void returnsEmptyForNonMatchingFilter() throws Exception {
        seedInTx("EVT-030", "LPPT", Instant.now());

        String xml = given()
                .queryParam("aerodromeDesignator", "ZZZZ")
                .when()
                .get("/swim/v1/features")
                .then()
                .statusCode(200)
                .extract().asString();

        assertThat(xml).contains("numberMatched=\"0\"");
    }
}
