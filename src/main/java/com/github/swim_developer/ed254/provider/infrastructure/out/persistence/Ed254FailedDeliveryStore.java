package com.github.swim_developer.ed254.provider.infrastructure.out.persistence;

import com.github.swim_developer.ed254.provider.domain.model.Ed254FailedDeliveryEntity;
import com.github.swim_developer.ed254.provider.infrastructure.out.persistence.entity.Ed254FailedDeliveryJpaEntity;
import com.github.swim_developer.framework.application.port.out.FailedDeliveryStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class Ed254FailedDeliveryStore
        implements PanacheRepository<Ed254FailedDeliveryJpaEntity>, FailedDeliveryStore<Ed254FailedDeliveryEntity> {

    private final Ed254ProviderPersistenceMapper mapper;

    @Inject
    public Ed254FailedDeliveryStore(Ed254ProviderPersistenceMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void persist(Ed254FailedDeliveryEntity domain) {
        Ed254FailedDeliveryJpaEntity jpa = mapper.toJpa(domain);
        persistAndFlush(jpa);
    }

    @Override
    public Ed254FailedDeliveryEntity createRecord(String eventId, UUID subscriptionId, String queue, String errorMessage) {
        return Ed254FailedDeliveryEntity.builder()
                .eventId(eventId)
                .subscriptionId(subscriptionId)
                .queue(queue)
                .errorMessage(errorMessage)
                .build();
    }

    @Override
    public List<Ed254FailedDeliveryEntity> findPendingRetries(int maxRetries, int batchSize) {
        return find("resolved = false AND retryCount < ?1 ORDER BY createdAt ASC", maxRetries)
                .page(0, batchSize).list().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Ed254FailedDeliveryEntity> findExceededRetries(int maxRetries, int batchSize) {
        return find("resolved = false AND retryCount >= ?1 ORDER BY createdAt ASC", maxRetries)
                .page(0, batchSize).list().stream().map(mapper::toDomain).toList();
    }

    @Override
    public long countPendingByEventId(String eventId) {
        return count("eventId = ?1 AND resolved = false", eventId);
    }
}
