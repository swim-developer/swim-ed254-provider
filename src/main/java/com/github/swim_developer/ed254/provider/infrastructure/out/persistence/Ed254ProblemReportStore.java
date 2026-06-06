package com.github.swim_developer.ed254.provider.infrastructure.out.persistence;

import com.github.swim_developer.ed254.provider.domain.model.Ed254ProblemReport;
import com.github.swim_developer.ed254.provider.domain.port.out.Ed254ProblemReportPort;
import com.github.swim_developer.ed254.provider.infrastructure.out.persistence.entity.Ed254ProblemReportJpaEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class Ed254ProblemReportStore implements PanacheRepositoryBase<Ed254ProblemReportJpaEntity, UUID>,
        Ed254ProblemReportPort {

    private final Ed254ProviderPersistenceMapper mapper;

    @Inject
    public Ed254ProblemReportStore(Ed254ProviderPersistenceMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void persist(Ed254ProblemReport domain) {
        Ed254ProblemReportJpaEntity jpa = mapper.toJpa(domain);
        persistAndFlush(jpa);
    }
}
