package com.github.swim_developer.ed254.provider.unit;

import com.github.swim_developer.ed254.provider.domain.model.Ed254EventMetadata;
import com.github.swim_developer.ed254.provider.infrastructure.out.xml.Ed254EventExtractor;
import com.github.swim_developer.framework.infrastructure.testing.TestNameLoggerExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(TestNameLoggerExtension.class)
@Timeout(value = 2, unit = TimeUnit.MINUTES)
class Ed254EventExtractorTest {

    private Ed254EventExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new Ed254EventExtractor();
    }

    @Test
    void shouldExtractMetadataFromArrivalSequencePayload() {
        String payload = """
                <arrivalSequence xmlns="http://coopans.org/swim/ed254/arrivalSequence/1.0">
                    <creationTime>2025-01-15T10:00:00Z</creationTime>
                    <publicationTime>2025-01-15T10:00:01.123Z</publicationTime>
                    <firstMessageAfterServiceOutage>false</firstMessageAfterServiceOutage>
                    <aerodromeDesignator>ESSA</aerodromeDesignator>
                    <sequenceEntries/>
                </arrivalSequence>
                """;

        var results = extractor.extract(payload);
        Optional<Ed254EventMetadata> result = results.isEmpty() ? Optional.empty() : results.get(0);

        assertThat(result).isPresent();
        assertThat(result.get().getAerodromeDesignator()).isEqualTo("ESSA");
        assertThat(result.get().getMessageType()).isEqualTo("ARRIVAL_SEQUENCE");
        assertThat(result.get().getPublicationTime()).isNotNull();
        assertThat(result.get().getCreationTime()).isNotNull();
    }

    @Test
    void shouldExtractMetadataFromProviderExceptionPayload() {
        String payload = """
                <providerExceptions xmlns="http://coopans.org/swim/ed254/arrivalSequence/1.0">
                    <provException>AMAN_UNAVAILABLE</provException>
                </providerExceptions>
                """;

        var results = extractor.extract(payload);
        Optional<Ed254EventMetadata> result = results.isEmpty() ? Optional.empty() : results.get(0);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldDetectArrivalSequenceMessageType() {
        String payload = "<arrivalSequence><aerodromeDesignator>ENGM</aerodromeDesignator></arrivalSequence>";

        var results = extractor.extract(payload);
        Optional<Ed254EventMetadata> result = results.isEmpty() ? Optional.empty() : results.get(0);

        assertThat(result).isPresent();
        assertThat(result.get().getMessageType()).isEqualTo("ARRIVAL_SEQUENCE");
    }

    @Test
    void shouldReturnEmptyForInvalidXml() {
        var results = extractor.extract("not xml at all");
        Optional<Ed254EventMetadata> result = results.isEmpty() ? Optional.empty() : results.get(0);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnSingleElementList() {
        String payload = "<arrivalSequence><aerodromeDesignator>TEST</aerodromeDesignator></arrivalSequence>";

        var results = extractor.extract(payload);

        assertThat(results).hasSize(1);
    }

    @Test
    void shouldHandleNullPayload() {
        var results = extractor.extract(null);

        assertThat(results).isNotNull();
    }

    @Test
    void shouldHandleEmptyPayload() {
        var results = extractor.extract("");

        assertThat(results).isNotNull();
    }
}
