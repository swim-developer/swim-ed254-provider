package com.github.swim_developer.ed254.provider.unit;

import com.github.swim_developer.ed254.provider.domain.model.Ed254DestinationAerodrome;
import com.github.swim_developer.ed254.provider.domain.model.Ed254SubscriptionCommand;
import com.github.swim_developer.ed254.provider.domain.model.Ed254SubscriptionFilters;
import com.github.swim_developer.ed254.provider.domain.model.Ed254SupplementaryData;
import com.github.swim_developer.ed254.provider.infrastructure.out.subscription.Ed254SubscriptionHashCalculator;
import com.github.swim_developer.framework.domain.model.QualityOfService;
import com.github.swim_developer.framework.infrastructure.testing.TestNameLoggerExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(TestNameLoggerExtension.class)
@Timeout(value = 2, unit = TimeUnit.MINUTES)
class Ed254SubscriptionHashCalculatorTest {

    private Ed254SubscriptionHashCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new Ed254SubscriptionHashCalculator();
    }

    @Test
    void sameRequestSameUserProducesSameHash() {
        var request = buildRequest(List.of("EHAM"), List.of("SUGOL"), List.of("09R"),
                false, QualityOfService.AT_LEAST_ONCE);

        String hash1 = calculator.calculateHash(request, "user1");
        String hash2 = calculator.calculateHash(request, "user1");

        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void differentUserProducesDifferentHash() {
        var request = buildRequest(List.of("EHAM"), List.of("SUGOL"), null, false, null);

        assertThat(calculator.calculateHash(request, "user1"))
                .isNotEqualTo(calculator.calculateHash(request, "user2"));
    }

    @Test
    void listOrderIsIrrelevantForHash() {
        var req1 = buildRequest(List.of("EHAM", "LFPG"), List.of("SUGOL", "RIVER"), null, false, null);
        var req2 = buildRequest(List.of("LFPG", "EHAM"), List.of("RIVER", "SUGOL"), null, false, null);

        assertThat(calculator.calculateHash(req1, "user1"))
                .isEqualTo(calculator.calculateHash(req2, "user1"));
    }

    @Test
    void differentAerodromesProduceDifferentHash() {
        var req1 = buildRequest(List.of("EHAM"), null, null, false, null);
        var req2 = buildRequest(List.of("LFPG"), null, null, false, null);

        assertThat(calculator.calculateHash(req1, "user1"))
                .isNotEqualTo(calculator.calculateHash(req2, "user1"));
    }

    @Test
    void supplementaryDataAffectsHash() {
        var req1 = buildRequest(List.of("EHAM"), null, null, false, null);
        var req2 = buildRequest(List.of("EHAM"), null, null, true, null);

        assertThat(calculator.calculateHash(req1, "user1"))
                .isNotEqualTo(calculator.calculateHash(req2, "user1"));
    }

    @Test
    void qosAffectsHash() {
        var req1 = buildRequest(List.of("EHAM"), null, null, false, QualityOfService.AT_LEAST_ONCE);
        var req2 = buildRequest(List.of("EHAM"), null, null, false, QualityOfService.EXACTLY_ONCE);

        assertThat(calculator.calculateHash(req1, "user1"))
                .isNotEqualTo(calculator.calculateHash(req2, "user1"));
    }

    @Test
    void nullQosProducesConsistentHash() {
        var request = buildRequest(List.of("EHAM"), null, null, false, null);

        String hash1 = calculator.calculateHash(request, "user1");
        String hash2 = calculator.calculateHash(request, "user1");

        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void hashIsSha256HexString() {
        var request = buildRequest(List.of("EHAM"), null, null, false, null);
        String hash = calculator.calculateHash(request, "user1");

        assertThat(hash).hasSize(64).matches("^[0-9a-f]{64}$");
    }

    @Test
    void nullFiltersProduceConsistentHash() {
        var request = new Ed254SubscriptionCommand(null, null, null, null);

        String hash1 = calculator.calculateHash(request, "user1");
        String hash2 = calculator.calculateHash(request, "user1");

        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void runwayDirectionsAffectHash() {
        var req1 = buildRequest(List.of("EHAM"), null, List.of("09R"), false, null);
        var req2 = buildRequest(List.of("EHAM"), null, List.of("27L"), false, null);

        assertThat(calculator.calculateHash(req1, "user1"))
                .isNotEqualTo(calculator.calculateHash(req2, "user1"));
    }

    private Ed254SubscriptionCommand buildRequest(List<String> aerodromes, List<String> pointNames,
                                                   List<String> runways, boolean anySupplementary,
                                                   QualityOfService qos) {
        List<Ed254DestinationAerodrome> destAerodromes = aerodromes != null
                ? aerodromes.stream()
                    .map(a -> Ed254DestinationAerodrome.builder()
                            .aerodromeDesignator(a)
                            .assignedArrivalRunway(runways)
                            .build())
                    .toList()
                : null;

        return new Ed254SubscriptionCommand(
                Ed254SubscriptionFilters.builder()
                        .destinationAerodrome(destAerodromes)
                        .pointName(pointNames)
                        .build(),
                Ed254SupplementaryData.builder()
                        .delay(anySupplementary)
                        .build(),
                qos,
                null);
    }
}
