package com.github.swim_developer.ed254.provider.domain.port.out;

import com.github.swim_developer.ed254.provider.domain.model.Ed254ProblemReport;

public interface Ed254ProblemReportPort {

    void persist(Ed254ProblemReport report);
}
