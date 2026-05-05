package com.github.swim_developer.ed254.provider.unit;

import com.github.swim_developer.ed254.provider.infrastructure.out.mapper.Ed254SubscriptionMapper;
import com.github.swim_developer.ed254.provider.domain.model.Ed254DestinationAerodrome;
import com.github.swim_developer.ed254.provider.domain.model.Ed254Subscription;
import com.github.swim_developer.ed254.provider.domain.model.Ed254SubscriptionCommand;
import com.github.swim_developer.ed254.provider.domain.model.Ed254SubscriptionFilters;
import com.github.swim_developer.ed254.provider.domain.model.Ed254SubscriptionResult;
import com.github.swim_developer.ed254.provider.domain.model.Ed254SupplementaryData;
import com.github.swim_developer.framework.domain.model.QualityOfService;
import com.github.swim_developer.framework.domain.model.SubscriptionStatus;
import com.github.swim_developer.framework.infrastructure.testing.TestNameLoggerExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(TestNameLoggerExtension.class)
@Timeout(value = 2, unit = TimeUnit.MINUTES)
class Ed254SubscriptionMapperTest {

    private final Ed254SubscriptionMapper mapper = new Ed254SubscriptionMapper();

    @Test
    void shouldMapRequestToEntityWithAllFields() {
        Ed254SubscriptionCommand request = buildRequest(
                List.of("ESSA", "ENGM"), List.of("AMAN1"), List.of("01L", "01R"),
                true, true, false, false, false,
                QualityOfService.AT_LEAST_ONCE);

        Ed254Subscription entity = mapper.toEntity(request, "user1", "ed254.user1.abc", Duration.ofHours(24));

        assertThat(entity.getUserId()).isEqualTo("user1");
        assertThat(entity.getQueue()).isEqualTo("ed254.user1.abc");
        assertThat(entity.getStatus()).isEqualTo(SubscriptionStatus.PAUSED);
        assertThat(entity.getQos()).isEqualTo(QualityOfService.AT_LEAST_ONCE);
        assertThat(entity.getAerodromes()).containsExactly("ESSA", "ENGM");
        assertThat(entity.getPointNames()).containsExactly("AMAN1");
        assertThat(entity.getRunwayDirections()).containsExactly("01L", "01R", "01L", "01R");
        assertThat(entity.isSupDelay()).isTrue();
        assertThat(entity.isSupLandingSequencePosition()).isTrue();
        assertThat(entity.isSupAmanStrategy()).isFalse();
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getSubscriptionEnd()).isAfter(entity.getCreatedAt());
    }

    @Test
    void toEntitySetsSubscriptionEndBasedOnTtl() {
        Ed254SubscriptionCommand request = new Ed254SubscriptionCommand(null, null, null, null);
        Instant before = Instant.now();

        Ed254Subscription entity = mapper.toEntity(request, "user1", "q1", Duration.ofHours(48));

        assertThat(entity.getSubscriptionEnd()).isAfter(before.plus(Duration.ofHours(47)));
        assertThat(entity.getSubscriptionEnd()).isBefore(before.plus(Duration.ofHours(49)));
    }

    @Test
    void shouldMapEntityToResult() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Ed254Subscription entity = Ed254Subscription.builder()
                .subscriptionId(id)
                .userId("user1")
                .queue("ed254.user1.abc")
                .status(SubscriptionStatus.ACTIVE)
                .qos(QualityOfService.AT_LEAST_ONCE)
                .aerodromes(List.of("ESSA"))
                .pointNames(List.of())
                .runwayDirections(List.of())
                .createdAt(now)
                .subscriptionEnd(now.plus(Duration.ofHours(24)))
                .build();

        Ed254SubscriptionResult result = mapper.toResult(entity, "TestProvider");

        assertThat(result.subscriptionId()).isEqualTo(id);
        assertThat(result.queueName()).isEqualTo("ed254.user1.abc");
        assertThat(result.subscriptionStatus()).isEqualTo("ACTIVE");
        assertThat(result.subscriptionResult()).isEqualTo("SUBSCRIPTION_SUCCESSFUL");
        assertThat(result.providerName()).isEqualTo("TestProvider");
        assertThat(result.heartbeatQueue()).isEqualTo("ed254.user1.abc-heartbeat");
        assertThat(result.subscriptionEnd()).isNotNull();
        assertThat(result.errorReason()).isNull();
    }

    @Test
    void shouldHandleNullFiltersInRequest() {
        Ed254SubscriptionCommand request = new Ed254SubscriptionCommand(null, null, QualityOfService.AT_MOST_ONCE, null);

        Ed254Subscription entity = mapper.toEntity(request, "user2", "ed254.user2.xyz", Duration.ofHours(24));

        assertThat(entity.getAerodromes()).isEmpty();
        assertThat(entity.getPointNames()).isEmpty();
        assertThat(entity.getRunwayDirections()).isEmpty();
        assertThat(entity.isSupDelay()).isFalse();
    }

    @Test
    void toEntityCreatesDefensiveCopiesOfLists() {
        var aerodromes = new java.util.ArrayList<>(List.of(
                Ed254DestinationAerodrome.builder().aerodromeDesignator("EHAM").build()));

        Ed254SubscriptionCommand request = new Ed254SubscriptionCommand(
                Ed254SubscriptionFilters.builder()
                        .destinationAerodrome(aerodromes)
                        .build(),
                null, null, null);

        Ed254Subscription entity = mapper.toEntity(request, "u", "q", Duration.ofHours(1));
        aerodromes.add(Ed254DestinationAerodrome.builder().aerodromeDesignator("LFPG").build());

        assertThat(entity.getAerodromes()).containsExactly("EHAM");
    }

    private Ed254SubscriptionCommand buildRequest(List<String> aerodromes, List<String> pointNames,
                                                   List<String> runways, boolean delay,
                                                   boolean landingSeqPos, boolean amanStrategy,
                                                   boolean depAerodrome, boolean proposedProc,
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
                        .delay(delay)
                        .landingSequencePosition(landingSeqPos)
                        .amanStrategy(amanStrategy)
                        .departureAerodrome(depAerodrome)
                        .proposedProcedure(proposedProc)
                        .build(),
                qos,
                null);
    }
}
