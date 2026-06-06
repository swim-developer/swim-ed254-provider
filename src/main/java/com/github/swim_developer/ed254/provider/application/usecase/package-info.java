/**
 * Core ED-254 provider services: ingestion validation, event extraction, AMQP publishing, Kafka routing,
 * subscription business logic, heartbeat and expiry integration with the SWIM framework.
 * <p>
 * Components plug into framework abstractions ({@link com.github.swim_developer.framework.application.port.out.SwimPayloadValidator},
 * {@link com.github.swim_developer.framework.application.port.out.SwimEventExtractor}, {@link com.github.swim_developer.framework.application.port.out.SwimOutboxRouter},
 * {@link com.github.swim_developer.framework.provider.application.subscription.AbstractEventDeliveryService},
 * {@link com.github.swim_developer.framework.provider.application.subscription.AbstractProviderSubscriptionService}) for a consistent
 * provider implementation.
 * </p>
 *
 * <h2>Expiry and heartbeat</h2>
 * <p>
 * {@link com.github.swim_developer.ed254.service.Ed254ExpiryStrategy} supplies ED-254-specific discovery of expired
 * subscriptions and terminated rows eligible for purge. Lifecycle transitions: ACTIVE or PAUSED subscriptions past
 * {@code subscriptionEnd} become TERMINATED; after a purge delay, terminated subscriptions are removed. Scheduling
 * and metrics are handled by {@link com.github.swim_developer.framework.provider.application.subscription.SubscriptionExpiryScheduler}.
 * </p>
 * <p>
 * {@link com.github.swim_developer.ed254.service.Ed254SubscriptionHeartbeatPublisher} publishes JSON
 * {@link com.github.swim_developer.framework.domain.model.SubscriptionHeartbeat} to per-subscription heartbeat queues
 * ({@code {queue}-heartbeat}). {@link com.github.swim_developer.ed254.service.Ed254ActiveSubscriptionSupplier}
 * enumerates active subscriptions for the framework scheduler.
 * </p>
 *
 * <h2>Package Contents</h2>
 * <table border="1">
 * <caption>Components</caption>
 * <tr><th>Component</th><th>Type</th><th>Purpose</th></tr>
 * <tr><td>{@link com.github.swim_developer.ed254.service.Ed254AmqpPublisher}</td><td>Class</td><td>Publishes payloads to subscriber queues with resilience annotations</td></tr>
 * <tr><td>{@link com.github.swim_developer.ed254.service.Ed254EventDeliveryUseCase}</td><td>Class</td><td>Delivers stored events to active subscriptions via AMQP</td></tr>
 * <tr><td>{@link com.github.swim_developer.ed254.service.Ed254EventExtractor}</td><td>Class</td><td>StAX extraction of aerodrome and timing fields from ED-254 XML</td></tr>
 * <tr><td>{@link com.github.swim_developer.ed254.service.Ed254ExpiryStrategy}</td><td>Class</td><td>Framework {@link com.github.swim_developer.framework.application.port.out.SubscriptionExpiryStrategy} for ED-254</td></tr>
 * <tr><td>{@link com.github.swim_developer.ed254.service.Ed254SubscriptionHeartbeatPublisher}</td><td>Class</td><td>Publishes JSON SubscriptionHeartbeat to {queue}-heartbeat</td></tr>
 * <tr><td>{@link com.github.swim_developer.ed254.service.Ed254ActiveSubscriptionSupplier}</td><td>Class</td><td>Supplies active subscriptions for heartbeat scheduling</td></tr>
 * <tr><td>{@link com.github.swim_developer.ed254.service.Ed254KafkaRouter}</td><td>Class</td><td>Routes messages to Kafka outbox and dead-letter channels</td></tr>
 * <tr><td>{@link com.github.swim_developer.ed254.service.Ed254PayloadValidator}</td><td>Class</td><td>Optional XSD validation of inbound ED-254 XML</td></tr>
 * <tr><td>{@link com.github.swim_developer.ed254.service.Ed254ProblemReportUseCase}</td><td>Class</td><td>Persists and meters CommunicateProblems payloads</td></tr>
 * <tr><td>{@link com.github.swim_developer.ed254.service.Ed254SubscriptionHashCalculator}</td><td>Class</td><td>Deterministic hash for idempotent subscription matching</td></tr>
 * <tr><td>{@link com.github.swim_developer.ed254.service.Ed254SubscriptionUseCase}</td><td>Class</td><td>Provider subscription CRUD, renew, terminate, and purge</td></tr>
 * </table>
 *
 * @since 1.0.0
 */
package com.github.swim_developer.ed254.provider.application.usecase;
