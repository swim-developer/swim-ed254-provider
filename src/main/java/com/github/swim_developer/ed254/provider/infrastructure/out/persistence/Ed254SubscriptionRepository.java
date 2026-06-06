package com.github.swim_developer.ed254.provider.infrastructure.out.persistence;

import com.github.swim_developer.ed254.provider.domain.model.Ed254Subscription;
import com.github.swim_developer.ed254.provider.domain.port.out.Ed254SubscriptionStore;
import com.github.swim_developer.ed254.provider.infrastructure.out.persistence.entity.Ed254SubscriptionJpaEntity;
import com.github.swim_developer.framework.domain.model.SubscriptionStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class Ed254SubscriptionRepository implements PanacheRepositoryBase<Ed254SubscriptionJpaEntity, UUID>, Ed254SubscriptionStore {

    private final Ed254ProviderPersistenceMapper mapper;

    @Inject
    public Ed254SubscriptionRepository(Ed254ProviderPersistenceMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void persist(Ed254Subscription domain) {
        Ed254SubscriptionJpaEntity jpa = mapper.toJpa(domain);
        if (jpa.getSubscriptionId() == null) {
            persistAndFlush(jpa);
            domain.setSubscriptionId(jpa.getSubscriptionId());
        } else {
            getEntityManager().merge(jpa);
        }
    }

    @Override
    public void delete(Ed254Subscription domain) {
        findByIdOptional(domain.getSubscriptionId()).ifPresent(this::delete);
    }

    @Override
    public Optional<Ed254Subscription> findSubscriptionById(UUID id) {
        return findByIdOptional(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Ed254Subscription> findEntityById(UUID id) {
        return findSubscriptionById(id);
    }

    @Override
    public Optional<Ed254Subscription> findByHash(String hash) {
        return findBySubscriptionHash(hash);
    }

    @Override
    public List<Ed254Subscription> findAllSubscriptions() {
        return findAll().list().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Ed254Subscription> findByStatus(SubscriptionStatus status) {
        return list("status", status).stream().map(mapper::toDomain).toList();
    }

    public List<Ed254Subscription> findByStatuses(SubscriptionStatus... statuses) {
        return list("status in ?1", List.of(statuses)).stream().map(mapper::toDomain).toList();
    }

    public List<Ed254Subscription> findByUserId(String userId) {
        return list("userId", userId).stream().map(mapper::toDomain).toList();
    }

    public Optional<Ed254Subscription> findByQueue(String queue) {
        return find("queue", queue).firstResultOptional().map(mapper::toDomain);
    }

    public long countByStatus(SubscriptionStatus status) {
        return count("status", status);
    }

    public List<Ed254Subscription> findBySubscriptionEndBefore(Instant threshold) {
        return list("subscriptionEnd < ?1 and (status = ?2 or status = ?3)",
                    threshold, SubscriptionStatus.ACTIVE, SubscriptionStatus.PAUSED)
                .stream().map(mapper::toDomain).toList();
    }

    public List<Ed254Subscription> findByStatusAndUpdatedAtBefore(SubscriptionStatus status, Instant threshold) {
        return list("status = ?1 and updatedAt < ?2", status, threshold)
                .stream().map(mapper::toDomain).toList();
    }

    public Optional<Ed254Subscription> findBySubscriptionHash(String subscriptionHash) {
        return find("subscriptionHash = ?1 and (status = ?2 or status = ?3)",
                subscriptionHash, SubscriptionStatus.ACTIVE, SubscriptionStatus.PAUSED)
                .firstResultOptional().map(mapper::toDomain);
    }

    public Optional<Ed254Subscription> findActiveOrPausedByQueueAndUser(String queueName, String userId) {
        return find("queue = ?1 and userId = ?2 and (status = ?3 or status = ?4)",
                queueName, userId, SubscriptionStatus.ACTIVE, SubscriptionStatus.PAUSED)
                .firstResultOptional().map(mapper::toDomain);
    }

    public boolean existsActiveOrPausedByQueue(String queueName) {
        return count("queue = ?1 and (status = ?2 or status = ?3)",
                queueName, SubscriptionStatus.ACTIVE, SubscriptionStatus.PAUSED) > 0;
    }
}
