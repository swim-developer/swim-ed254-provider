package com.github.swim_developer.ed254.provider.application.port.out;

import com.github.swim_developer.ed254.provider.domain.model.Ed254SubscriptionCommand;

public interface Ed254SubscriptionHashPort {

    String calculateHash(Ed254SubscriptionCommand command, String userId);
}
