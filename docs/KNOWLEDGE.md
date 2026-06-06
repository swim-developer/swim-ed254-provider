# swim-ed254-provider — Knowledge Base

## What This Is

**ATSU/AISP role.** Publishes ED-254 (EUROCAE) Arrival Sequence data to downstream ANSP consumers via AMQP 1.0. Built on the swim-developer-framework; implements only ED-254-specific domain logic.

~66 classes. 30 unit + 20 integration tests.

## Standard

**EUROCAE ED-254** — Arrival Sequence Service Performance Standard.

Protocol requirements: EUROCONTROL SPEC-170 (SWIM-TI Yellow Profile) — AMQP 1.0 over TLS 1.3, REST/HTTP.

## REST API

| Endpoint | Description |
|----------|-------------|
| `POST /swim/v1/subscriptions` | Create subscription |
| `GET /swim/v1/subscriptions/{id}` | Get subscription |
| `PUT /swim/v1/subscriptions/{id}` | Update subscription (ACTIVE/PAUSED) |
| `DELETE /swim/v1/subscriptions/{id}` | Delete subscription |
| `GET /swim/v1/topics` | List available topics |
| `GET /swim/v1/features` | WFS GetFeature |
| `POST /swim/v1/communicate-problems` | ED-254 conformance endpoint |
| `POST /swim/v1/unsubscription-response` | ED-254 conformance endpoint |

## Architecture

```
com.github.swim_developer.ed254.provider
├── domain/model/        Subscription, Topic, ArrivalSequence
├── application/usecase/ Subscription and event use cases
└── infrastructure/      rest, scheduling, persistence/PostgreSQL, amqp, kafka, artemis
```

## Data Model

**FIXM 4.3** — Flight Information Exchange Model, ED-254 extension.

Dependency: `swim-fixm-model-ed254`

## Kafka Topics (input)

| Topic | Purpose |
|-------|---------|
| `ed254-arrival-sequence-topic` | Incoming arrival sequence events |
| `ed254-provider-exception-topic` | Exception/DLQ events |

## Framework SPIs Implemented

| SPI | Implementation |
|-----|---------------|
| `SwimSubscription<E>` | Subscription contract + filter logic |
| `SwimPayloadValidator` | ED-254 / FIXM XSD validation |
| `SwimIngressHandler` | Kafka ingestion |

## Framework Components Used

| Class | Usage |
|-------|-------|
| `AbstractEventDeliveryService` | Load subscriptions → filter → publish to AMQP |
| `AbstractAmqpPublisher` | Artemis AMQP publishing |
| `PerSubscriptionHeartbeatScheduler` | Heartbeat to `{queue}.heartbeat` |
| `SwimSubscriptionExpiryScheduler` | Auto-purge expired subscriptions |
| `ArtemisJmxQueueProvisioner` / `KubernetesQueueProvisioner` | Dev vs prod queue provisioning |

## PostgreSQL

- DB: `swim-ed254`
- Table: `subscriptions`

## Build & Run

```bash
cd ../swim-developer-framework && mvn clean install -DskipTests
./mvnw clean package -DskipTests
./mvnw quarkus:dev
./mvnw verify -DskipITs=false
```

Local infra: `podman compose up -d` (see repo root `compose.yml`)
