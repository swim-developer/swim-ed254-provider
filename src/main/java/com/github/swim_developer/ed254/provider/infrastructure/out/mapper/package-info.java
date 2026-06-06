/**
 * Maps ED-254 subscription API payloads to persistence entities and responses.
 * <p>
 * Static conversion helpers align REST request bodies with {@link com.github.swim_developer.ed254.model.Ed254Subscription}
 * and {@link com.github.swim_developer.ed254.model.Ed254SubscriptionResponse}, applying defaults such as initial
 * subscription end time from configured TTL.
 * </p>
 *
 * <h2>Package Contents</h2>
 * <table border="1">
 * <caption>Components</caption>
 * <tr><th>Component</th><th>Type</th><th>Purpose</th></tr>
 * <tr><td>{@link com.github.swim_developer.ed254.mapper.Ed254SubscriptionMapper}</td><td>Class</td><td>Maps between subscription request, entity, and response</td></tr>
 * </table>
 *
 * @since 1.0.0
 */
package com.github.swim_developer.ed254.provider.infrastructure.out.mapper;
