package com.github.swim_developer.ed254.provider.infrastructure.out.persistence;

import com.github.swim_developer.ed254.provider.domain.model.Ed254EventEntity;
import com.github.swim_developer.ed254.provider.domain.model.EventQueryFilters;
import com.github.swim_developer.ed254.provider.domain.port.out.Ed254EventStore;
import com.github.swim_developer.ed254.provider.infrastructure.out.persistence.entity.Ed254EventJpaEntity;
import com.github.swim_developer.framework.domain.model.EventStatus;
import com.github.swim_developer.framework.domain.model.SwimProviderEvent;
import com.github.swim_developer.framework.provider.application.port.out.SwimProviderEventStorePort;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class Ed254EventRepository implements PanacheRepositoryBase<Ed254EventJpaEntity, String>, Ed254EventStore, SwimProviderEventStorePort {

    private final Ed254ProviderPersistenceMapper mapper;

    @Inject
    public Ed254EventRepository(Ed254ProviderPersistenceMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void persist(Ed254EventEntity domain) {
        Ed254EventJpaEntity jpa = mapper.toJpa(domain);
        persistAndFlush(jpa);
    }

    @Override
    public void update(Ed254EventEntity domain) {
        Ed254EventJpaEntity jpa = mapper.toJpa(domain);
        getEntityManager().merge(jpa);
    }

    @Override
    public Ed254EventEntity findDomainById(String id) {
        return findByIdOptional(id).map(mapper::toDomain).orElse(null);
    }

    public Ed254EventEntity mergeDomainEntity(Ed254EventEntity domain) {
        Ed254EventJpaEntity jpa = mapper.toJpa(domain);
        Ed254EventJpaEntity merged = getEntityManager().merge(jpa);
        return mapper.toDomain(merged);
    }

    @Override
    public Optional<Ed254EventEntity> findByEventId(String eventId) {
        return find("eventId", eventId).firstResultOptional().map(mapper::toDomain);
    }

    @Override
    public long count() {
        return count("1 = 1");
    }

    public List<Ed254EventEntity> findByStatus(EventStatus status) {
        return list("status", status).stream().map(mapper::toDomain).toList();
    }

    public List<Ed254EventEntity> findPendingDelivery(int limit) {
        return find("status in (?1, ?2) ORDER BY receivedAt ASC",
                EventStatus.RECEIVED, EventStatus.PARTIALLY_DELIVERED)
                .page(0, limit).list().stream().map(mapper::toDomain).toList();
    }

    public long countByStatus(EventStatus status) {
        return count("status", status);
    }

    public List<Ed254EventEntity> findWithFilters(EventQueryFilters filters) {
        List<String> conditions = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();

        if (filters.aerodromeDesignator() != null && !filters.aerodromeDesignator().isBlank()) {
            conditions.add("aerodromeDesignator = :aerodrome");
            params.put("aerodrome", filters.aerodromeDesignator());
        }
        if (filters.messageType() != null && !filters.messageType().isBlank()) {
            conditions.add("messageType = :messageType");
            params.put("messageType", filters.messageType());
        }
        if (filters.startTime() != null) {
            conditions.add("receivedAt >= :startTime");
            params.put("startTime", filters.startTime());
        }
        if (filters.endTime() != null) {
            conditions.add("receivedAt <= :endTime");
            params.put("endTime", filters.endTime());
        }

        String query = conditions.isEmpty()
                ? "ORDER BY receivedAt DESC"
                : String.join(" and ", conditions) + " ORDER BY receivedAt DESC";

        return find(query, params)
                .page(filters.startIndex(), filters.count())
                .list()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
