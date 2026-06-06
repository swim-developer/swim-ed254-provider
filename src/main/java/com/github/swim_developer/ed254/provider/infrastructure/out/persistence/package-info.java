/**
 * Panache repositories for ED-254 provider persistence.
 * <p>
 * Queries support subscription lifecycle (active/paused/terminated), deduplication by message id, and scheduled
 * expiry: subscriptions whose {@code subscriptionEnd} is before a threshold (for termination) and terminated
 * rows older than a threshold (for purge and resource cleanup).
 * </p>
 *
 * <h2>Package Contents</h2>
 * <table border="1">
 * <caption>Components</caption>
 * <tr><th>Component</th><th>Type</th><th>Purpose</th></tr>
 * <tr><td>{@link com.github.swim_developer.ed254.provider.infrastructure.out.persistence.Ed254EventRepository}</td><td>Class</td><td>Event entities by status, message id, and pending delivery batches</td></tr>
 * <tr><td>{@link com.github.swim_developer.ed254.provider.infrastructure.out.persistence.Ed254ProblemReportStore}</td><td>Class</td><td>Persistence for problem reports</td></tr>
 * <tr><td>{@link com.github.swim_developer.ed254.provider.infrastructure.out.persistence.Ed254SubscriptionRepository}</td><td>Class</td><td>Subscriptions by user, queue, hash, status, and expiry-oriented queries</td></tr>
 * </table>
 *
 * @since 1.0.0
 */
package com.github.swim_developer.ed254.provider.infrastructure.out.persistence;
