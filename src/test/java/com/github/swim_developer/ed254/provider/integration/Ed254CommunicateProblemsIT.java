package com.github.swim_developer.ed254.provider.integration;

import com.github.swim_developer.framework.domain.model.DataValidationResult;
import com.github.swim_developer.framework.domain.model.ErrorCode;
import com.github.swim_developer.framework.domain.model.ErrorDetail;
import com.github.swim_developer.framework.domain.model.ValidationResultType;
import com.github.swim_developer.framework.provider.infrastructure.out.security.JwtRoleValidator;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import com.github.swim_developer.framework.infrastructure.testing.TestNameLoggerExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestSecurity(user = "it-test-user", roles = "user")
@ExtendWith(TestNameLoggerExtension.class)
@Timeout(value = 2, unit = TimeUnit.MINUTES)
class Ed254CommunicateProblemsIT {

    private static final String PROBLEMS_PATH = "/arrivalSequenceInformation/v1/problems";

    @InjectMock
    JwtRoleValidator jwtRoleValidator;

    @Test
    void communicateProblemsReturns202ForSequenceGaps() {
        DataValidationResult body = DataValidationResult.sequenceGaps(List.of("TAP1234", "RYR5678"));

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(PROBLEMS_PATH)
                .then()
                .statusCode(202);
    }

    @Test
    void communicateProblemsReturns202ForWrongFormat() {
        DataValidationResult body = DataValidationResult.wrongFormat("Invalid XML structure");

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(PROBLEMS_PATH)
                .then()
                .statusCode(202);
    }

    @Test
    void communicateProblemsReturns202ForDataInvalid() {
        DataValidationResult body = DataValidationResult.dataInvalid(
                ErrorCode.TIME_ACCURACY, "creationTime", "Timestamp exceeds threshold");

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(PROBLEMS_PATH)
                .then()
                .statusCode(202);
    }

    @Test
    void communicateProblemsReturns202ForNonSubscribedData() {
        DataValidationResult body = DataValidationResult.nonSubscribedData("Aerodrome ZZZZ not subscribed");

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(PROBLEMS_PATH)
                .then()
                .statusCode(202);
    }

    @Test
    void communicateProblemsReturns202WithNullErrorReport() {
        var body = Map.of("dataValResult", "DATA_INVALID");

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(PROBLEMS_PATH)
                .then()
                .statusCode(202);
    }

    @Test
    void communicateProblemsReturns202WithMultipleErrors() {
        DataValidationResult body = new DataValidationResult(
                ValidationResultType.DATA_INVALID,
                List.of(
                        new ErrorDetail(ErrorCode.TIME_ACCURACY, "creationTime", "Stale"),
                        new ErrorDetail(ErrorCode.WRONG_ENUM_VALUE, "messageType", "Unknown type"),
                        new ErrorDetail(ErrorCode.PATTERN_VIOLATION, "arcid", "Invalid format")
                )
        );

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(PROBLEMS_PATH)
                .then()
                .statusCode(202);
    }

    @Test
    void communicateProblemsReturns202WithEmptyErrorReport() {
        var body = Map.of(
                "dataValResult", "SEQUENCE_GAPS",
                "errorReport", List.of()
        );

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(PROBLEMS_PATH)
                .then()
                .statusCode(202);
    }
}
