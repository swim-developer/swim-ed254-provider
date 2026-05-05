/**
 * JAX-RS resources for ED-254 arrival sequence subscription and problem reporting APIs.
 * <p>
 * Exposes paths under {@code /arrivalSequenceInformation/v1} for creating and managing subscriptions and for
 * consumers to submit data validation results (CommunicateProblems). OpenAPI annotations describe operations and
 * response codes.
 * </p>
 *
 * <h2>Package Contents</h2>
 * <table border="1">
 * <caption>Components</caption>
 * <tr><th>Component</th><th>Type</th><th>Purpose</th></tr>
 * <tr><td>{@link com.github.swim_developer.ed254.resource.Ed254ProblemsResource}</td><td>Class</td><td>POST endpoint for CommunicateProblems (ED-254 consumer feedback)</td></tr>
 * <tr><td>{@link com.github.swim_developer.ed254.resource.Ed254SubscriptionResource}</td><td>Class</td><td>CRUD and lifecycle for arrival sequence subscriptions</td></tr>
 * </table>
 *
 * @since 1.0.0
 */
package com.github.swim_developer.ed254.provider.infrastructure.in.rest;
