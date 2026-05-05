package com.github.swim_developer.ed254.provider.infrastructure.out.xml;

import com.github.swim_developer.ed254.provider.domain.model.Ed254EventMetadata;
import com.github.swim_developer.framework.application.port.out.SwimEventExtractor;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import com.github.swim_developer.framework.infrastructure.out.xml.SafeXmlFactory;

import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@ApplicationScoped
public class Ed254EventExtractor implements SwimEventExtractor<Ed254EventMetadata, String> {

    @Override
    public List<Optional<Ed254EventMetadata>> extract(String rawPayload) {
        if (rawPayload == null || rawPayload.isBlank()) {
            log.warn("Received null or blank ED-254 payload");
            return List.of(Optional.empty());
        }
        try {
            XMLStreamReader reader = SafeXmlFactory.xmlInputFactory().createXMLStreamReader(new StringReader(rawPayload));
            String aerodrome = null;
            String publicationTime = null;
            String creationTime = null;
            String messageType = determineMessageType(rawPayload);

            while (reader.hasNext()) {
                reader.next();
                if (reader.isStartElement()) {
                    String localName = reader.getLocalName();
                    switch (localName) {
                        case "aerodromeDesignator" -> aerodrome = reader.getElementText();
                        case "publicationTime" -> publicationTime = reader.getElementText();
                        case "creationTime" -> creationTime = reader.getElementText();
                        default -> log.trace("Skipping ED-254 element: {}", localName);
                    }
                }
                if (aerodrome != null && publicationTime != null && creationTime != null) {
                    break;
                }
            }
            reader.close();

            if (aerodrome == null) {
                log.warn("No aerodromeDesignator found in ED-254 payload");
                return List.of(Optional.empty());
            }

            return List.of(Optional.of(Ed254EventMetadata.builder()
                    .aerodromeDesignator(aerodrome)
                    .messageType(messageType)
                    .publicationTime(publicationTime != null ? Instant.parse(publicationTime) : null)
                    .creationTime(creationTime != null ? Instant.parse(creationTime) : null)
                    .build()));
        } catch (Exception e) {
            log.error("Failed to extract ED-254 metadata", e);
            return List.of(Optional.empty());
        }
    }

    private String determineMessageType(String payload) {
        if (payload.contains("arrivalSequence")) return "ARRIVAL_SEQUENCE";
        if (payload.contains("providerExceptions")) return "PROVIDER_EXCEPTION";
        return "UNKNOWN";
    }
}
