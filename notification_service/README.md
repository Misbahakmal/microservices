# Notification Service

A Spring Boot microservice responsible for consuming order events from Apache Kafka and storing notifications in the database.

## Features

- Kafka Consumer
- Consumes order events
- Stores notifications in PostgreSQL
- Event-driven architecture
- Clean (Hexagonal) Architecture
- Spring Data JPA
- Docker-ready

---

# Architecture

```
com.demo.notification_service
├── domain/
│   └── model/
│       └── Notification
│
├── application/
│   ├── port/
│   │   ├── in/
│   │   └── out/
│   │       └── NotificationRepositoryPort
│   │
│   └── usecase/
│       └── NotificationService
│
└── infrastructure/
    ├── adapter/
    │   ├── in/
    │   │   └── messaging/
    │   │       └── OrderEventConsumer
    │   │
    │   └── out/
    │       └── persistence/
    │           ├── NotificationEntity
    │           └── NotificationPersistenceAdapter
    │
    └── config/
        └── KafkaConfig
```

---

# Technology Stack

- Java 17
- Spring Boot 4
- Spring Kafka
- Spring Data JPA
- PostgreSQL
- Apache Kafka
- Docker
- Maven
- Lombok

---

# Responsibilities

The Notification Service is responsible for:

- Listening to Kafka order events
- Processing incoming order events
- Creating notification records
- Persisting notifications in PostgreSQL

---

# Event Flow

```
Order Service
      │
      │ Publish Event
      ▼
+------------------+
|   Kafka Topic    |
|   order-events   |
+------------------+
      │
      │ Consume Event
      ▼
Notification Service
      │
      ▼
Save Notification
      │
      ▼
 PostgreSQL
```

---

# Run the Application

Build the project

```bash
./mvnw clean compile
```

Run the application

```bash
./mvnw spring-boot:run
```

---

# Docker

Start all services

```bash
docker compose up -d
```

Stop all services

```bash
docker compose down -v
```

---

# View Kafka Logs

```bash
docker logs customer_service-kafka-1
```

---

# Kafka Configuration

**Topic**

```
order-events
```

The service listens for order events published by the Order Service.

---

# Example Event

```json
{
  "eventId": "uuid",
  "orderId": "uuid",
  "occurredAt": "2026-07-12T20:30:00Z",
  "status": "CREATED"
}
```

---

# Verify Consumed Events

Run inside the Kafka container:

```bash
docker exec -it customer_service-kafka-1 \
kafka-console-consumer \
--bootstrap-server localhost:9092 \
--topic order-events \
--from-beginning
```

---
