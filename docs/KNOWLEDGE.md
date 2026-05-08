# swim-ed254-provider — Knowledge Base


## What This Is

**ATSU/AISP role.** Publishes ED-254 (EUROCAE) Arrival Sequence data to downstream ANSP consumers via AMQP 1.0. Same framework and REST API shape as DNOTAM provider, different data model (FIXM 4.3) and additional ED-254-specific endpoints.

~66 classes. 30 unit + 20 integration tests.

## Differences vs DNOTAM Provider

| Aspect | DNOTAM Provider | ED-254 Provider |
|--------|-----------------|-----------------|
| Data model | AIXM 5.1.1 | FIXM 4.3 |
| Standard | SWIM Registry / SPEC-170 | EUROCAE ED-254 |
| Kafka input | 6 DNOTAM topics | `ed254-arrival-sequence-topic`, `ed254-provider-exception-topic` |
| Extra endpoints | — | `CommunicateProblems`, `UnsubscriptionResponse` (ED-254 conformance) |

## REST API

Same structure as DNOTAM provider (`/swim/v1/subscriptions`, `/swim/v1/topics`, `/swim/v1/features`) plus ED-254-specific conformance endpoints.

## Architecture

```
com.github.swim_developer.ed254.provider
├── domain/model/        Subscription, Topic, ArrivalSequence
├── application/usecase/ Subscription and event use cases
└── infrastructure/      (rest, scheduling, persistence/PostgreSQL, amqp, kafka, artemis)
```

## Framework Wiring

Same as DNOTAM provider. Replace `dnotam` with `ed254` in class names.

| Framework Class | Usage |
|-----------------|--------|
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
quarkus dev
./mvnw verify -DskipITs=false
```

Local infra: `podman compose up -d` (requires a compose.yml with Kafka, MongoDB/PostgreSQL, Artemis — see repo root)`
