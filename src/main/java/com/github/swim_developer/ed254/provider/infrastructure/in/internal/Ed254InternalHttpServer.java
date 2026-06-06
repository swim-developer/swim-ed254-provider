package com.github.swim_developer.ed254.provider.infrastructure.in.internal;

import com.github.swim_developer.ed254.provider.domain.model.Ed254Subscription;
import com.github.swim_developer.ed254.provider.infrastructure.out.persistence.Ed254EventRepository;
import com.github.swim_developer.ed254.provider.infrastructure.out.persistence.Ed254SubscriptionRepository;
import com.github.swim_developer.ed254.provider.infrastructure.in.amqp.Ed254IngressMessageHandler;
import com.github.swim_developer.ed254.provider.infrastructure.out.xml.Ed254PayloadValidator;
import com.github.swim_developer.framework.infrastructure.out.cluster.LeaderElection;
import com.github.swim_developer.framework.domain.model.ValidationResult;
import com.github.swim_developer.framework.domain.model.EventStatus;
import com.github.swim_developer.framework.domain.model.SubscriptionStatus;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mutiny.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;
import java.util.List;

@ApplicationScoped
@Slf4j
public class Ed254InternalHttpServer {

    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String MEDIA_JSON = "application/json";
    private static final String KEY_VALID = "valid";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_STATUS = "status";

    private final Vertx vertx;
    private final Ed254IngressMessageHandler eventProcessor;
    private final Ed254PayloadValidator payloadValidator;
    private final Ed254SubscriptionRepository subscriptionRepository;
    private final Ed254EventRepository eventRepository;
    private final LeaderElection leaderElection;
    private final int port;

    private HttpServer server;

    @Inject
    public Ed254InternalHttpServer(Vertx vertx,
            Ed254IngressMessageHandler eventProcessor,
            Ed254PayloadValidator payloadValidator,
            Ed254SubscriptionRepository subscriptionRepository,
            Ed254EventRepository eventRepository,
            LeaderElection leaderElection,
            @ConfigProperty(name = "internal.server.port", defaultValue = "9080") int port) {
        this.vertx = vertx;
        this.eventProcessor = eventProcessor;
        this.payloadValidator = payloadValidator;
        this.subscriptionRepository = subscriptionRepository;
        this.eventRepository = eventRepository;
        this.leaderElection = leaderElection;
        this.port = port;
    }

    void onStart(@Observes StartupEvent ev) {
        Router router = Router.router(vertx.getDelegate());
        router.route().handler(BodyHandler.create());

        router.post("/internal/v1/trigger").handler(this::handleTrigger);
        router.post("/internal/v1/validate").handler(this::handleValidate);
        router.get("/internal/v1/subscriptions/summary").handler(this::handleSubscriptionsSummary);
        router.get("/internal/v1/status").handler(this::handleStatus);

        server = vertx.getDelegate().createHttpServer()
                .requestHandler(router)
                .listen(port)
                .result();

        log.info("ED-254 internal HTTP server started on port {}", port);
    }

    void onStop(@Observes ShutdownEvent ev) {
        if (server != null) {
            server.close();
            log.info("ED-254 internal HTTP server stopped");
        }
    }

    private void handleTrigger(RoutingContext ctx) {
        String payload = ctx.body().asString();

        if (payload == null || payload.isBlank()) {
            sendError(ctx, 400, "Empty or missing XML body");
            return;
        }

        io.vertx.core.Vertx core = vertx.getDelegate();
        core.getOrCreateContext().executeBlocking(() -> {
            eventProcessor.processEvent(payload);
            return null;
        }, false).onComplete(ar -> {
            if (ar.succeeded()) {
                sendSuccess(ctx, 202, "Event accepted for processing");
            } else {
                Throwable cause = ar.cause();
                log.error("Error processing triggered event", cause);
                String msg = cause != null && cause.getMessage() != null ? cause.getMessage() : "Internal error";
                sendError(ctx, 500, msg);
            }
        });
    }

    private void handleValidate(RoutingContext ctx) {
        String payload = ctx.body().asString();

        if (payload == null || payload.isBlank()) {
            sendError(ctx, 400, "Empty or missing XML body");
            return;
        }

        io.vertx.core.Vertx core = vertx.getDelegate();
        core.getOrCreateContext().executeBlocking(() -> {
            ValidationResult result = payloadValidator.validate(payload);
            if (result.valid()) {
                return new JsonObject()
                        .put(KEY_VALID, true)
                        .put(KEY_MESSAGE, "ED-254 message is valid against XSD schema");
            }
            return new JsonObject()
                    .put(KEY_VALID, false)
                    .put(KEY_MESSAGE, String.join("; ", result.errors()));
        }, false).onComplete(ar -> {
            if (ar.succeeded()) {
                JsonObject result = ar.result();
                int status = Boolean.TRUE.equals(result.getBoolean(KEY_VALID)) ? 200 : 422;
                result.put(KEY_TIMESTAMP, Instant.now().toString());
                ctx.response().setStatusCode(status)
                        .putHeader(HEADER_CONTENT_TYPE, MEDIA_JSON)
                        .end(result.encode());
            } else {
                sendError(ctx, 500, ar.cause().getMessage());
            }
        });
    }

    private void handleSubscriptionsSummary(RoutingContext ctx) {
        io.vertx.core.Vertx core = vertx.getDelegate();
        core.getOrCreateContext().executeBlocking(() -> {
            List<Ed254Subscription> active = subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE);
            List<Ed254Subscription> paused = subscriptionRepository.findByStatus(SubscriptionStatus.PAUSED);

            return new JsonObject()
                    .put("totalActive", active.size())
                    .put("totalPaused", paused.size())
                    .put("subscribers", buildSubscriberList(active));
        }, false).onComplete(ar -> {
            if (ar.succeeded()) {
                JsonObject result = ar.result();
                result.put(KEY_TIMESTAMP, Instant.now().toString());
                ctx.response().setStatusCode(200)
                        .putHeader(HEADER_CONTENT_TYPE, MEDIA_JSON)
                        .end(result.encode());
            } else {
                sendError(ctx, 500, ar.cause().getMessage());
            }
        });
    }

    private void handleStatus(RoutingContext ctx) {
        io.vertx.core.Vertx core = vertx.getDelegate();
        core.getOrCreateContext().executeBlocking(() -> {
            long activeSubscriptions = subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);
            long received = eventRepository.countByStatus(EventStatus.RECEIVED);
            long delivered = eventRepository.countByStatus(EventStatus.DELIVERED);
            long partiallyDelivered = eventRepository.countByStatus(EventStatus.PARTIALLY_DELIVERED);
            long deadLetter = eventRepository.countByStatus(EventStatus.DEAD_LETTER);
            long totalEvents = eventRepository.count();

            return new JsonObject()
                    .put(KEY_STATUS, "UP")
                    .put("leader", leaderElection.isLeader())
                    .put("hostname", leaderElection.getHostname())
                    .put("xsdValidation", true)
                    .put("subscriptions", new JsonObject()
                            .put("active", activeSubscriptions))
                    .put("events", new JsonObject()
                            .put("total", totalEvents)
                            .put("received", received)
                            .put("delivered", delivered)
                            .put("partiallyDelivered", partiallyDelivered)
                            .put("deadLetter", deadLetter));
        }, false).onComplete(ar -> {
            if (ar.succeeded()) {
                JsonObject result = ar.result();
                result.put(KEY_TIMESTAMP, Instant.now().toString());
                ctx.response().setStatusCode(200)
                        .putHeader(HEADER_CONTENT_TYPE, MEDIA_JSON)
                        .end(result.encode());
            } else {
                ctx.response().setStatusCode(503)
                        .putHeader(HEADER_CONTENT_TYPE, MEDIA_JSON)
                        .end(new JsonObject()
                                .put(KEY_STATUS, "DOWN")
                                .put("error", ar.cause().getMessage())
                                .put(KEY_TIMESTAMP, Instant.now().toString())
                                .encode());
            }
        });
    }

    private JsonArray buildSubscriberList(List<Ed254Subscription> subscriptions) {
        JsonArray array = new JsonArray();
        for (Ed254Subscription sub : subscriptions) {
            array.add(new JsonObject()
                    .put("subscriptionId", sub.getSubscriptionId().toString())
                    .put("userId", sub.getUserId())
                    .put("queue", sub.getQueue())
                    .put("aerodromes", sub.getAerodromes() != null ? new JsonArray(sub.getAerodromes()) : new JsonArray())
                    .put("qos", sub.getQos().name()));
        }
        return array;
    }

    private void sendSuccess(RoutingContext ctx, int status, String message) {
        ctx.response()
                .setStatusCode(status)
                .putHeader(HEADER_CONTENT_TYPE, MEDIA_JSON)
                .end(new JsonObject()
                        .put(KEY_STATUS, "accepted")
                        .put(KEY_MESSAGE, message)
                        .put(KEY_TIMESTAMP, Instant.now().toString())
                        .encode());
    }

    private void sendError(RoutingContext ctx, int status, String error) {
        ctx.response()
                .setStatusCode(status)
                .putHeader(HEADER_CONTENT_TYPE, MEDIA_JSON)
                .end(new JsonObject()
                        .put("error", error)
                        .put(KEY_TIMESTAMP, Instant.now().toString())
                        .encode());
    }
}
