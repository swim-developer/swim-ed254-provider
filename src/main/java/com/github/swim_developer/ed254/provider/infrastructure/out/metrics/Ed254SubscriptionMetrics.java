package com.github.swim_developer.ed254.provider.infrastructure.out.metrics;

import com.github.swim_developer.ed254.provider.domain.model.Ed254Subscription;
import com.github.swim_developer.ed254.provider.infrastructure.out.persistence.Ed254SubscriptionRepository;
import com.github.swim_developer.framework.infrastructure.out.cluster.LeaderElection;
import com.github.swim_developer.framework.domain.model.SubscriptionStatus;
import com.github.swim_developer.framework.provider.application.metrics.AbstractSubscriptionMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MultiGauge;
import io.micrometer.core.instrument.Tags;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class Ed254SubscriptionMetrics extends AbstractSubscriptionMetrics {

    private final Ed254SubscriptionRepository subscriptionRepository;
    private final LeaderElection leaderElection;

    private MultiGauge subscriptionsByAerodrome;
    private MultiGauge subscriptionsByQos;

    protected Ed254SubscriptionMetrics() {
        this(null, null, null);
    }

    @Inject
    protected Ed254SubscriptionMetrics(MeterRegistry registry,
                                       Ed254SubscriptionRepository subscriptionRepository,
                                       LeaderElection leaderElection) {
        super(registry);
        this.subscriptionRepository = subscriptionRepository;
        this.leaderElection = leaderElection;
    }

    @Override
    protected String getServiceName() {
        return "ed254";
    }

    @Override
    protected double countActiveSubscriptions() {
        try {
            return subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);
        } catch (Exception e) {
            log.warn("Failed to count active ED-254 subscriptions", e);
            return 0;
        }
    }

    @Override
    protected void registerCustomGauges() {
        subscriptionsByAerodrome = MultiGauge.builder("ed254_subscriptions_by_aerodrome")
                .description("Active ED-254 subscriptions by aerodrome filter")
                .register(registry);

        subscriptionsByQos = MultiGauge.builder("ed254_subscriptions_by_qos")
                .description("Active ED-254 subscriptions by Quality of Service level")
                .register(registry);
    }

    @Override
    protected void performGaugeUpdate() {
        List<Ed254Subscription> active = subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE);
        updateAerodromeGauges(active);
        updateQosGauges(active);
    }

    void onStart(@Observes StartupEvent ev) {
        updateGauges();
    }

    @Scheduled(every = "30s")
    void scheduledUpdate() {
        if (!leaderElection.isLeader()) {
            return;
        }
        updateGauges();
    }

    private void updateAerodromeGauges(List<Ed254Subscription> subscriptions) {
        Map<String, Long> counts = subscriptions.stream()
                .filter(s -> s.getAerodromes() != null && !s.getAerodromes().isEmpty())
                .flatMap(s -> s.getAerodromes().stream())
                .collect(Collectors.groupingBy(aerodrome -> aerodrome, Collectors.counting()));

        subscriptionsByAerodrome.register(counts.entrySet().stream()
                .map(e -> MultiGauge.Row.of(Tags.of("aerodrome", e.getKey()), e.getValue()))
                .toList(), true);
    }

    private void updateQosGauges(List<Ed254Subscription> subscriptions) {
        Map<String, Long> counts = subscriptions.stream()
                .filter(s -> s.getQos() != null)
                .collect(Collectors.groupingBy(
                        s -> s.getQos().name(),
                        Collectors.counting()));

        subscriptionsByQos.register(counts.entrySet().stream()
                .map(e -> MultiGauge.Row.of(Tags.of("qos", e.getKey()), e.getValue()))
                .toList(), true);
    }
}
