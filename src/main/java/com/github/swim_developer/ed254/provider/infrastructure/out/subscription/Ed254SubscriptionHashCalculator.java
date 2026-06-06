package com.github.swim_developer.ed254.provider.infrastructure.out.subscription;

import com.github.swim_developer.ed254.provider.application.port.out.Ed254SubscriptionHashPort;
import com.github.swim_developer.ed254.provider.domain.model.Ed254SubscriptionCommand;
import com.github.swim_developer.ed254.provider.domain.model.Ed254SupplementaryData;
import com.github.swim_developer.framework.application.service.AbstractSubscriptionHashCalculator;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class Ed254SubscriptionHashCalculator extends AbstractSubscriptionHashCalculator<Ed254SubscriptionCommand>
        implements Ed254SubscriptionHashPort {

    @Override
    public String calculateHash(Ed254SubscriptionCommand command, String userId) {
        StringBuilder sb = new StringBuilder();

        sb.append("userId:").append(userId).append("|");
        sb.append("aerodromes:").append(sortedListToString(command.extractAerodromeDesignators())).append("|");
        sb.append("pointNames:").append(sortedListToString(command.extractPointNames())).append("|");
        sb.append("runways:").append(sortedListToString(command.extractRunwayDesignators())).append("|");

        Ed254SupplementaryData sup = command.supplementaryData();
        if (sup != null) {
            sb.append("sup:").append(sup.anyRequested());
        } else {
            sb.append("sup:false");
        }
        sb.append("|");

        sb.append("qos:").append(command.qos() != null ? command.qos().name() : "");

        String data = sb.toString();
        log.debug("Calculating hash for: {}", data);

        return sha256(data);
    }
}
