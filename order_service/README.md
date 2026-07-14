# Order Service

A Spring Boot microservice responsible for managing customer orders in a microservices-based e-commerce system.

## Features

- Create orders
- Retrieve orders
- Update order status
- Customer validation via Customer Service
- Event publishing using Apache Kafka
- Service discovery with Eureka
- PostgreSQL persistence
- Idempotent order creation
- OpenAPI-first REST API
- Clean Architecture (Hexagonal Architecture)

---

# Architecture

```
com.demo.order_service
├── domain/
│   └── model/
│       ├── Order
│       ├── OrderItem
│       ├── OrderEvent
│       └── OrderStatus
│
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── CreateOrderUseCase
│   │   │   ├── GetOrderUseCase
│   │   │   └── UpdateOrderStatusUseCase
│   │   │
│   │   └── out/
│   │       ├── OrderRepositoryPort
│   │       ├── OrderEventPublisherPort
│   │       └── CustomerServicePort
│   │
│   └── usecase/
│       └── OrderService
│
└── infrastructure/
    ├── adapter/
    │   ├── in/
    │   │   └── web/
    │   │       └── OrderController
    │   │
    │   └── out/
    │       ├── persistence/
    │       │   ├── OrderEntity
    │       │   └── OrderPersistenceAdapter
    │       │
    │       ├── messaging/
    │       │   └── KafkaOrderEventPublisher
    │       │
    │       └── rest/
    │           └── CustomerServiceRestAdapter
    │
    └── config/
        └── InfrastructureConfig
```

---

# Technology Stack

- Java 17
- Spring Boot 4
- Spring Data JPA
- Spring Cloud Netflix Eureka
- Spring WebClient
- Apache Kafka
- PostgreSQL
- Docker
- Docker Compose
- OpenAPI Generator
- MapStruct
- Lombok

---

# Getting Started

## Start Docker

```bash
docker compose up -d
```
---

## Build the Project

```bash
./mvnw clean compile
```

or run directly

```bash
./mvnw spring-boot:run
```
---

# REST API

| Method | Endpoint | Description |
|---------|----------|-------------|
| POST | `/orders` | Create a new order |
| GET | `/orders` | Get all orders |
| GET | `/orders/{id}` | Get order by ID |
| PATCH | `/orders/{id}/status` | Update order status |

---

# Service URLs

Order Service

```
GET http://localhost:8082/orders

GET http://localhost:8082/orders/{id}
```
---

# Create Order
- To create order first copy valid customer address and addressId from customer service and then create order for that customer.
### Headers

```
Idempotency-Key: 550e8400-e29b-41d4-a716-446655440000
Content-Type: application/json
```

### Request

```json
{
  "customerId": "5624c38f-ef19-4715-9d98-91f5ee49f63b",
  "addressId": "be504958-cea6-4192-a39c-3cb9d3fcc8b2",
  "items": [
    {
      "productId": "7858c241-1191-4963-8d07-28564177d452",
      "productName": "Monitor",
      "quantity": 1,
      "unitPrice": 10.00
    }
  ]
}
```

---

# Update Order Status

```
PATCH /orders/{orderId}/status
```

### Request Body

```json
{
  "status": "SHIPPED"
}
```

---

# Order Status State Machine

```
           CREATED
          /       \
         /         \
        ▼           ▼
   SHIPPED     CANCELLED
```

Only the following transitions are allowed:

- CREATED → SHIPPED
- CREATED → CANCELLED

All other transitions return **HTTP 400 Bad Request**.

---

# Kafka Integration

## Topic

```
order-events
```

### Partitions

```
3
```
---

# Verify Published Events

Run inside the Kafka container:

```bash
docker exec -it customer_service-kafka-1 \
kafka-console-consumer \
--bootstrap-server localhost:9092 \
--topic order-events \
--from-beginning
```

---

# Create Kafka Topic

Run inside the Kafka container:

```bash
kafka-topics \
--create \
--topic order-events \
--partitions 3 \
--bootstrap-server localhost:9092
```

---

# Describe Topic

```bash
kafka-topics \
--describe \
--topic order-events \
--bootstrap-server localhost:9092
```

---
