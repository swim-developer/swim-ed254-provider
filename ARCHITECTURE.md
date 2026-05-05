# swim-ed254-provider — Architecture

> Diagrams use [Mermaid](https://mermaid.js.org) and render natively on GitHub.

**Role**: ED-254 Arrival Sequence Provider — exposes a subscription REST API per EUROCAE ED-254, receives arrival sequence events from Kafka, validates FIXM 4.3 payloads, and delivers them to subscriber AMQP queues. Also accepts `DataValidationResult` problem reports from downstream ATSUs (ED-254 REQ-0160).

---

## 1. System Context (C4 Level 1)

```mermaid
C4Context
    title System Context — swim-ed254-provider

    Person(atsu, "ATSU / Consumer", "Subscribes to Arrival Sequence topics, receives events via AMQP, submits problem reports")
    Person(operator, "Provider Operator", "Configures the provider and monitors service health")

    System(provider, "swim-ed254-provider", "ED-254 Provider: subscription management REST API + AMQP arrival sequence delivery + problem report handling")

    System_Ext(kafka, "Apache Kafka", "Source of arrival sequence events (upstream ingestion pipeline)")
    System_Ext(broker, "AMQP Broker", "ActiveMQ Artemis — AMQP 1.0 / mTLS — per-subscriber queues")
    System_Ext(postgres, "PostgreSQL", "Subscription, event and problem report persistence")

    Rel(atsu, provider, "Manages subscriptions, receives events, submits problem reports", "REST / HTTPS / mTLS + AMQP 1.0 / mTLS")
    Rel(provider, kafka, "Consumes incoming arrival sequence events")
    Rel(provider, broker, "Publishes events to per-subscriber queues", "AMQP 1.0 / mTLS")
    Rel(provider, postgres, "Persists subscriptions, events and problem reports")
    Rel(operator, provider, "Monitors and manages", "REST / HTTPS")

    UpdateLayoutConfig($c4ShapeInRow="3", $c4BoundaryInRow="1")
```

---

## 2. Container Diagram (C4 Level 2)

```mermaid
C4Container
    title Container Diagram — swim-ed254-provider

    Person(atsu, "ATSU / Consumer")
    Person(operator, "Provider Operator")

    System_Ext(kafka, "Apache Kafka", "Incoming arrival sequence event source")
    System_Ext(broker, "AMQP Broker", "ActiveMQ Artemis — AMQP 1.0 / mTLS")

    System_Boundary(sys, "swim-ed254-provider") {
        Container(app, "swim-ed254-provider", "Quarkus / Java 21", "ED-254 subscription REST API, FIXM 4.3 validation, event delivery, heartbeat, expiry, problem report handling")
        ContainerDb(postgres, "PostgreSQL", "Relational DB", "Subscriptions, arrival events and problem reports")
    }

    Rel(atsu, app, "Manages subscriptions and submits problem reports", "REST / HTTPS / mTLS")
    Rel(operator, app, "Monitors service", "REST / HTTPS")
    Rel(app, kafka, "Consumes incoming arrival sequence events")
    Rel(app, broker, "Publishes to subscriber queues", "AMQP 1.0 / mTLS")
    Rel(app, postgres, "Persists subscriptions, events and problem reports")
```

---

## 3. Component Diagram (C4 Level 3)

```mermaid
C4Component
    title Component Diagram — swim-ed254-provider

    System_Ext(kafka, "Apache Kafka")
    System_Ext(broker, "AMQP Broker")
    System_Ext(postgres, "PostgreSQL")

    Container_Boundary(provider, "swim-ed254-provider") {
        Component(subRes, "Ed254SubscriptionResource", "JAX-RS", "REST — subscription creation, update and deletion")
        Component(problemsRes, "Ed254ProblemsResource", "JAX-RS", "REST — receives DataValidationResult problem reports from subscribers (ED-254 REQ-0160)")
        Component(featureRes, "FeatureResource", "JAX-RS", "WFS GetFeature endpoint — queries persisted arrival events")
        Component(topicRes, "Ed254TopicResource", "JAX-RS", "REST — topic listing")

        Component(ingressHandler, "Ed254IngressMessageHandler", "SmallRye Messaging", "Kafka consumer — receives arrival sequence events, validates, delegates to delivery use case")

        Component(subUC, "Ed254SubscriptionUseCase", "CDI", "Subscriber registration, queue provisioning, update, deletion")
        Component(deliveryUC, "Ed254EventDeliveryUseCase", "CDI", "Fan-out: delivers arrival sequence event to all active subscriber AMQP queues")
        Component(queryUC, "Ed254EventQueryUseCase", "CDI", "WFS query over persisted arrival events")
        Component(problemUC, "Ed254ProblemReportUseCase", "CDI", "Processes and persists DataValidationResult problem reports from subscribers")

        Component(payloadValidator, "Ed254PayloadValidator", "JAXB / XSD / SwimPayloadValidator SPI", "Validates FIXM 4.3 payload against ED-254 XSD schema")
        Component(extractor, "Ed254EventExtractor", "CDI / SwimEventExtractor SPI", "Extracts event metadata from FIXM ED-254 message")
        Component(jaxbPool, "Ed254JaxbUnmarshallerPool", "JAXB", "Thread-safe pool of JAXB unmarshallers for FIXM 4.3 XML")

        Component(amqpPublisher, "Ed254AmqpPublisher", "Qpid JMS / SwimAmqpPublisherPort SPI", "Sends AMQP messages to individual subscriber queues")
        Component(heartbeat, "Ed254SubscriptionHeartbeatPublisher", "Quartz", "Publishes heartbeat messages to subscriber queues")
        Component(expiry, "Ed254ExpiryStrategy", "Quartz / SubscriptionExpiryStrategy SPI", "Marks subscriptions as expired when TTL is exceeded")

        Component(evtRepo, "Ed254EventRepository", "JPA / Ed254EventStore port/out", "Arrival event persistence")
        Component(subRepo, "Ed254SubscriptionRepository", "JPA / Ed254SubscriptionStore port/out", "Subscriber persistence")
        Component(failedStore, "Ed254FailedDeliveryStore", "JPA / SwimFailedDeliveryStorePort SPI", "Failed delivery persistence for retry")
        Component(problemStore, "Ed254ProblemReportStore", "JPA / Ed254ProblemReportPort port/out", "Problem report persistence")
    }

    Rel(subRes, subUC, "calls via", "ManageSubscriptionPort")
    Rel(problemsRes, problemUC, "calls via", "Ed254ProblemReportPort")
    Rel(featureRes, queryUC, "calls via", "QueryEventPort")
    Rel(ingressHandler, payloadValidator, "validates with")
    Rel(ingressHandler, extractor, "extracts metadata with")
    Rel(ingressHandler, deliveryUC, "delegates to via", "DeliverEventPort — after validation")
    Rel(deliveryUC, amqpPublisher, "publishes via", "SwimAmqpPublisherPort")
    Rel(deliveryUC, subRepo, "reads active subscribers via", "Ed254SubscriptionStore port")
    Rel(deliveryUC, evtRepo, "persists events via", "Ed254EventStore port")
    Rel(deliveryUC, failedStore, "stores failures via", "SwimFailedDeliveryStorePort")
    Rel(heartbeat, amqpPublisher, "sends heartbeats via", "SwimAmqpPublisherPort")
    Rel(expiry, subUC, "expires subscriptions")
    Rel(problemUC, problemStore, "persists via", "Ed254ProblemReportPort")

    Rel(ingressHandler, kafka, "consumes from")
    Rel(amqpPublisher, broker, "publishes to", "AMQP 1.0 / mTLS")
    Rel(evtRepo, postgres, "persists to")
    Rel(subRepo, postgres, "persists to")
    Rel(failedStore, postgres, "persists to")
    Rel(problemStore, postgres, "persists to")
```

---

## 4. Event Delivery — Sequence

```mermaid
sequenceDiagram
    autonumber
    participant Kafka as Apache Kafka
    participant Handler as Ed254IngressMessageHandler
    participant Validator as Ed254PayloadValidator
    participant Extractor as Ed254EventExtractor
    participant UC as Ed254EventDeliveryUseCase
    participant SubRepo as Ed254SubscriptionRepository
    participant Publisher as Ed254AmqpPublisher
    participant Broker as AMQP Broker
    participant EvtStore as Ed254EventRepository

    Kafka->>Handler: arrival sequence event (FIXM 4.3 XML)
    Handler->>Validator: validate(payload) via SwimPayloadValidator SPI
    Validator-->>Handler: valid / invalid
    Handler->>Extractor: extract(payload) via SwimEventExtractor SPI
    Extractor-->>Handler: event metadata
    Handler->>UC: deliver(event) via DeliverEventPort
    UC->>SubRepo: load active subscriptions
    SubRepo-->>UC: subscriber list
    loop for each active subscriber
        UC->>Publisher: publish(subscriberQueue, message)
        Publisher->>Broker: send (AMQP / mTLS)
    end
    UC->>EvtStore: persist(event)
    UC-->>Handler: delivery complete

    Note over Publisher,Broker: On delivery failure: stored in Ed254FailedDeliveryStore<br/>for retry by AbstractFailedDeliveryRecoveryScheduler (framework).
```

---

## 5. Problem Report Handling (ED-254 REQ-0160)

Downstream ATSUs (consumers) can submit `DataValidationResult` problem reports when they detect issues with received arrival sequence data.

```mermaid
sequenceDiagram
    autonumber
    actor ATSU as ATSU Consumer
    participant ProbRes as Ed254ProblemsResource
    participant ProbUC as Ed254ProblemReportUseCase
    participant ProbStore as Ed254ProblemReportStore

    ATSU->>ProbRes: POST /problems (DataValidationResult)
    ProbRes->>ProbUC: report(problemReport)
    ProbUC->>ProbStore: persist via Ed254ProblemReportPort
    ProbStore-->>ProbUC: ok
    ProbUC-->>ProbRes: ok
    ProbRes-->>ATSU: 201 Created
```
