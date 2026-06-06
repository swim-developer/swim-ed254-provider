package com.github.swim_developer.ed254.provider.unit;

import com.github.swim_developer.framework.domain.model.DataValidationResult;
import com.github.swim_developer.framework.domain.model.ErrorCode;
import com.github.swim_developer.framework.domain.model.ErrorDetail;
import com.github.swim_developer.framework.domain.model.ValidationResultType;
import com.github.swim_developer.framework.infrastructure.testing.TestNameLoggerExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(TestNameLoggerExtension.class)
@Timeout(value = 2, unit = TimeUnit.MINUTES)
class DataValidationResultTest {

    @Test
    void wrongFormatFactoryCreatesCorrectResult() {
        DataValidationResult result = DataValidationResult.wrongFormat("Invalid XML structure");

        assertThat(result.dataValResult()).isEqualTo(ValidationResultType.WRONG_FORMAT);
        assertThat(result.errorReport()).hasSize(1);
        assertThat(result.errorReport().get(0).errorCode()).isEqualTo(ErrorCode.NOT_READABLE);
        assertThat(result.errorReport().get(0).errorMessage()).isEqualTo("Invalid XML structure");
        assertThat(result.errorReport().get(0).erroneousFieldName()).isNull();
    }

    @Test
    void dataInvalidFactoryCreatesCorrectResult() {
        DataValidationResult result = DataValidationResult.dataInvalid(
                ErrorCode.TIME_ACCURACY, "creationTime", "Timestamp exceeds threshold");

        assertThat(result.dataValResult()).isEqualTo(ValidationResultType.DATA_INVALID);
        assertThat(result.errorReport()).hasSize(1);
        assertThat(result.errorReport().get(0).errorCode()).isEqualTo(ErrorCode.TIME_ACCURACY);
        assertThat(result.errorReport().get(0).erroneousFieldName()).isEqualTo("creationTime");
        assertThat(result.errorReport().get(0).errorMessage()).isEqualTo("Timestamp exceeds threshold");
    }

    @Test
    void nonSubscribedDataFactoryCreatesCorrectResult() {
        DataValidationResult result = DataValidationResult.nonSubscribedData("Aerodrome ZZZZ not subscribed");

        assertThat(result.dataValResult()).isEqualTo(ValidationResultType.NON_SUBSCRIBED_DATA);
        assertThat(result.errorReport()).hasSize(1);
        assertThat(result.errorReport().get(0).errorCode()).isEqualTo(ErrorCode.LOGIC_VIOLATION);
        assertThat(result.errorReport().get(0).errorMessage()).isEqualTo("Aerodrome ZZZZ not subscribed");
    }

    @Test
    void sequenceGapsFactoryCreatesCorrectResult() {
        DataValidationResult result = DataValidationResult.sequenceGaps(List.of("TAP1234", "RYR5678"));

        assertThat(result.dataValResult()).isEqualTo(ValidationResultType.SEQUENCE_GAPS);
        assertThat(result.errorReport()).hasSize(2);
        assertThat(result.errorReport()).extracting(ErrorDetail::errorCode)
                .containsOnly(ErrorCode.LOGIC_VIOLATION);
        assertThat(result.errorReport()).extracting(ErrorDetail::errorMessage)
                .allMatch(msg -> msg.contains("disappeared without lastFiledRecord=true"));
    }

    @Test
    void recordEquality() {
        DataValidationResult a = new DataValidationResult(
                ValidationResultType.WRONG_FORMAT,
                List.of(new ErrorDetail(ErrorCode.NOT_READABLE, null, "bad xml")));
        DataValidationResult b = new DataValidationResult(
                ValidationResultType.WRONG_FORMAT,
                List.of(new ErrorDetail(ErrorCode.NOT_READABLE, null, "bad xml")));

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
    }

    @Test
    void nullErrorReportIsAllowed() {
        DataValidationResult result = new DataValidationResult(
                ValidationResultType.DATA_INVALID, null);

        assertThat(result.dataValResult()).isEqualTo(ValidationResultType.DATA_INVALID);
        assertThat(result.errorReport()).isNull();
    }

    @Test
    void allValidationResultTypesExist() {
        assertThat(ValidationResultType.values()).containsExactly(
                ValidationResultType.SEQUENCE_GAPS,
                ValidationResultType.WRONG_FORMAT,
                ValidationResultType.DATA_INVALID,
                ValidationResultType.NON_SUBSCRIBED_DATA
        );
    }

    @Test
    void allErrorCodesExist() {
        assertThat(ErrorCode.values()).hasSize(15);
        assertThat(ErrorCode.values()).contains(
                ErrorCode.TIME_ZONE, ErrorCode.LOGIC_VIOLATION,
                ErrorCode.NOT_READABLE, ErrorCode.DATA_CORRUPTION
        );
    }
}
