# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Is

ED-254 (EUROCAE) Arrival Sequence Provider — AISP-role Quarkus service that exposes a Subscription Manager REST API, consumes arrival sequence events from Kafka, validates FIXM 4.3 payloads against XSD, and delivers them to subscriber AMQP queues in ActiveMQ Artemis. Built on the `swim-developer-framework`; implements only ED-254-specific domain logic.

Standard: EUROCAE ED-254 + EUROCONTROL SPEC-170 (SWIM-TI Yellow Profile).

## Build and Development Commands

```bash
# Build (compile + package, compiles tests but skips execution)
./mvnw clean package -DskipTests

# Run in dev mode (requires local infra running)
./mvnw quarkus:dev -Ddebug=false -Dquarkus.http.host=0.0.0.0

# Unit tests only
./mvnw test

# Unit + integration tests (ITs are skipped by default in pom.xml)
./mvnw verify -DskipITs=false

# Run a single test class
./mvnw test -Dtest=Ed254EventExtractorTest

# Run a single integration test
./mvnw verify -DskipITs=false -Dit.test=Ed254ProviderIT

# Start local infrastructure (Artemis, PostgreSQL, Kafka, Keycloak, Validator)
podman compose up -d

# Validate infrastructure services are healthy
./src/local-dev/validate.sh

# Generate local TLS certificates (run once or after rotation)
./certs/generate.sh

# Install sibling dependencies into local Maven repo
make sync
```

**NEVER use** `-Dmaven.test.skip=true` (skips test compilation). Always use `-DskipTests`.

## Architecture

Hexagonal (ports and adapters) inside `com.github.swim_developer.ed254.provider`:

```
domain/model/              Entities, value objects (Subscription, EventMetadata, FlightSelector, ProblemReport)
domain/port/out/           Output ports (Ed254SubscriptionStore, Ed254EventStore, Ed254ProblemReportPort)
application/port/in/       Input ports (ManageSubscriptionPort, QueryEventPort, Ed254SubscriptionConfig)
application/port/out/      Output ports (Ed254SubscriptionHashPort, Ed254SubscriptionMappingPort)
application/usecase/       Use cases (SubscriptionUseCase, EventDeliveryUseCase, EventQueryUseCase, ProblemReportUseCase)
infrastructure/in/rest/    JAX-RS resources (SubscriptionResource, TopicResource, ProblemsResource)
infrastructure/in/amqp/    Kafka consumer (Ed254IngressMessageHandler)
infrastructure/in/internal/ Vert.x HTTP server on port 9080 (event injection, validation, status)
infrastructure/out/        AMQP publisher, persistence (JPA/Panache), XML validation, Kafka router, metrics, mappers
```

### Event Flow

Kafka event -> `Ed254IngressMessageHandler` -> `Ed254PayloadValidator` (XSD) -> `Ed254EventExtractor` (metadata) -> `Ed254EventDeliveryUseCase` (fan-out) -> `Ed254AmqpPublisher` (per-subscriber queue in Artemis)

### Framework SPIs Implemented

| SPI | This project's implementation |
|-----|-------------------------------|
| `SwimPayloadValidator` | `Ed254PayloadValidator` — FIXM 4.3 XSD validation |
| `SwimEventExtractor` | `Ed254EventExtractor` — extracts arrival sequence metadata |
| `SwimAmqpPublisherPort` | `Ed254AmqpPublisher` — publishes to Artemis subscriber queues |
| `SubscriptionExpiryStrategy` | `Ed254ExpiryStrategy` — TTL-based subscription expiry |
| `SwimFailedDeliveryStorePort` | `Ed254FailedDeliveryStore` — failed delivery persistence for retry |

### Sibling Dependencies (must be installed first)

`swim-developer-root` -> `swim-fixm-model-ed254` -> `swim-developer-framework` -> `swim-developer-extensions`

Run `make sync` to clone and install all, or `make deps` to see the order.

## Testing

- Unit tests: `src/test/.../unit/` — standard JUnit 5 + Mockito
- Integration tests: `src/test/.../integration/` — `@QuarkusTest` with Testcontainers (Postgres, Artemis, Kafka)
- ITs are **skipped by default** (`<skipITs>true</skipITs>` in pom.xml) — always pass `-DskipITs=false`
- HTTP/API tests use **RestAssured** for requests and **AssertJ** for assertions
- Integration tests bind host ports — run ONE project at a time, never in parallel

## Key Rules

- **Naming must be unambiguous.** Always qualify names to differentiate from siblings (e.g., `swim-ed254-provider`, not `swim-provider`).

## Quarkus Profiles

- `dev` — `application-dev.properties` (local development with compose services)
- `test` — `application-test.properties` (Testcontainers/CI)
- `prod` — `application-prod.properties` (OpenShift/Kubernetes)

## Key Ports (dev mode)

| Port | Service |
|------|---------|
| 8080 | REST API (plain HTTP) |
| 8443 | REST API (HTTPS/mTLS) |
| 9080 | Internal API (event injection, no auth) |
| 5671 | Artemis AMQPS (mTLS) |
| 5672 | Artemis AMQP (plain) |
| 9092 | Kafka |
| 8543 | Keycloak (HTTPS) |

## Container Images

```bash
make jvm                 # JVM multi-arch image (fastest)
make native-amd64        # Native amd64
make native-arm64        # Native arm64
make manifest && make push  # Multi-arch manifest
```

Override registry/tag: `make jvm REGISTRY=quay.io/myorg TAG=v1.2.3`
