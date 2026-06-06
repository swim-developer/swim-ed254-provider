/**
 * Persistence entities and API DTOs for the ED-254 arrival sequence provider.
 * <p>
 * JPA entities store subscriptions, ingested events, and consumer problem reports. Request and response types
 * mirror the subscription REST contract. {@link com.github.swim_developer.ed254.model.Ed254EventMetadata} holds
 * extracted XML fields for routing and filtering.
 * </p>
 *
 * <h2>Subscription termination (ED-254)</h2>
 * <p>
 * Per EUROCAE ED-254 Section 6.3.2.2 (Initial Termination Time), subscriptions use an absolute end time
 * ({@code subscriptionEnd}), not a relative TTL alone; perpetual subscriptions are not allowed. The provider
 * terminates the subscription at that instant; consumers must renew before expiration.
 * </p>
 *
 * <h2>Package Contents</h2>
 * <table border="1">
 * <caption>Components</caption>
 * <tr><th>Component</th><th>Type</th><th>Purpose</th></tr>
 * <tr><td>{@link com.github.swim_developer.ed254.model.Ed254EventEntity}</td><td>Class</td><td>JPA entity for persisted arrival sequence event payloads and delivery state</td></tr>
 * <tr><td>{@link com.github.swim_developer.ed254.model.Ed254EventMetadata}</td><td>Class</td><td>Immutable metadata extracted from ED-254 XML for filtering</td></tr>
 * <tr><td>{@link com.github.swim_developer.ed254.model.Ed254ProblemReport}</td><td>Class</td><td>JPA entity for CommunicateProblems reports from consumers</td></tr>
 * <tr><td>{@link com.github.swim_developer.ed254.model.Ed254Subscription}</td><td>Class</td><td>JPA subscription implementing framework {@link com.github.swim_developer.framework.domain.model.SwimSubscription}</td></tr>
 * <tr><td>{@link com.github.swim_developer.ed254.model.Ed254SubscriptionRequest}</td><td>Class</td><td>Incoming create-subscription payload</td></tr>
 * <tr><td>{@link com.github.swim_developer.ed254.model.Ed254SubscriptionResponse}</td><td>Class</td><td>Subscription details returned by the API</td></tr>
 * </table>
 *
 * @since 1.0.0
 */
package com.github.swim_developer.ed254.provider.domain.model;
