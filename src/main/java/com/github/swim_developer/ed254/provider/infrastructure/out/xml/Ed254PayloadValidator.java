package com.github.swim_developer.ed254.provider.infrastructure.out.xml;

import aero.fixm.validation.Ed254XsdValidator;
import com.github.swim_developer.framework.domain.model.ValidationResult;
import com.github.swim_developer.framework.application.port.out.SwimPayloadValidator;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class Ed254PayloadValidator implements SwimPayloadValidator {

    private final Ed254XsdValidator xsdValidator;

    public Ed254PayloadValidator() {
        try {
            this.xsdValidator = new Ed254XsdValidator();
            log.info("ED-254 XSD validator initialized via fixm-ed254-model");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize ED-254 XSD validator", e);
        }
    }

    @Override
    public ValidationResult validate(String payload) {
        try {
            xsdValidator.validateAndUnmarshal(payload);
            return ValidationResult.ok();
        } catch (Ed254XsdValidator.ValidationException e) {
            return ValidationResult.fail(e.getMessage());
        }
    }
}
