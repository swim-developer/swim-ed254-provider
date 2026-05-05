package com.github.swim_developer.ed254.provider.domain.port.out;

import com.github.swim_developer.ed254.provider.domain.model.Ed254EventEntity;

import java.util.Optional;

public interface Ed254EventStore {

    void persist(Ed254EventEntity entity);

    void update(Ed254EventEntity entity);

    Ed254EventEntity findDomainById(String id);

    Optional<Ed254EventEntity> findByEventId(String eventId);
}
