# Microservices Kafka & API First
A Java Spring Boot microservices project demonstrating a scalable backend architecture using Spring Cloud, Docker, Apache Kafka, PostgreSQL, and Eureka Service Discovery.

## Project Overview

This project implements a simple e-commerce order processing system using a microservices architecture. It demonstrates service decomposition, inter-service communication, 
asynchronous messaging, and service discovery.

## Architecture

```
                    +--------------------+
                    |    Eureka Server   |
                    |      Port 8761     |
                    +---------+----------+
                              |
         ---------------------------------------------
         |                                           |
         |                                           |
+--------+--------+                     +------------+------------+
| Customer Service|                     |      Order Service      |
|    Port 8081    |<------------------->|        Port 8082        |
+--------+--------+                     +------------+------------+
         |                                           |
         |                                           |
         |                              Publishes Order Events
         |                                           |
         |                                      Apache Kafka
         |                                           |
         |                                           |
         |                              +------------+------------+
         |                              | Notification Service    |
         |                              | Kafka Consumer          |
         |                              +-------------------------+

                    PostgreSQL Database
```
---

## Services
- Customer Service — owns customer and address data. Exposes REST endpoints for other services to consume.
- Order Service — accepts orders, validates the customer/address synchronously via Feign + Eureka, and publishes order lifecycle events to Kafka.
- Notification Service — Kafka consumer only, no exposed REST API. Reacts to order events idempotently.
- Eureka Server — service discovery registry; services locate each other by logical name, not hardcoded URLs.
- Each service follows Hexagonal Architecture: domain → application (ports & use cases) → infrastructure (REST controllers, JPA adapters, Kafka producers/consumers).
---
### Running the Project
- Docker & Docker Compose
- Java 21 (only needed if building/running a service outside Docker)
  
### Clone Repository

```bash
git clone https://github.com/YOUR_USERNAME/YOUR_REPOSITORY.git
cd YOUR_REPOSITORY
```
---
### Start Docker Containers
```bash
docker compose up --build
```
---

### Verify Running Containers

```bash
docker ps
```
You should see containers for:
- postgres
- eureka-server
- customer-service
- order-service
- notification-service (if enabled)

---
## Service URLs

| Service | URL |
|----------|-----|
| Eureka Dashboard | http://localhost:8761 |
| Customer Service | http://localhost:8081 |
| Order Service | http://localhost:8082 |

---

## API Requests Endpoints
- GIVEN INSIDE EACH MICROSERVICE(Please check README file inside each service to run each service independently and to check its end points)

---

## Project Structure

```
Microservices/
├── docker-compose.yaml
├── init.sql                     # creates the 3 schemas + per-service DB users
├── eureka-server/
├── customer_service/
│   ├── openapi/customer-api.yaml
│   └── src/main/java/com/demo/customer_service/
│       ├── domain/
│       ├── application/{port/in, port/out, usecase}/
│       └── infrastructure/adapter/{in/web, out/persistence}/
├── order_service/
│   ├── openapi/order-api.yaml
│   └── src/main/java/com/demo/order_service/
│       ├── domain/
│       ├── application/{port/in, port/out, usecase}/
│       └── infrastructure/adapter/{in/web, out/persistence, out/rest}/
└── notification_service/
    └── src/main/java/com/demo/notification_service/
        ├── domain/
        ├── application/{port/in, port/out, usecase}/
        └── infrastructure/adapter/{out/persistence, out/messaging}/
```

---

## Tech Stack
- Language / Framework: Java 21, Spring Boot
- REST, OpenAPI 3.x (API First — spec generated into controller interfaces)
- Apache Kafka (KRaft mode, no Zookeeper)
- Netflix Eureka
- OpenFeign + Spring Cloud LoadBalancer
- PostgreSQL, one isolated schema per service, Spring Data JPA
- Docker, Docker Compose
  
---

## Future Improvements

- API Gateway
- Centralized Configuration Server
- JWT Authentication
- Monitoring with Prometheus & Grafana
- Unit tests for domain and use case layers
- Integration tests for REST controllers and Kafka consumers
- CI/CD Pipeline(GitHub Actions): build, test, and lint on every push
- Kubernetes Deployment

---
## Learning Objectives
This project was built to practice:

- Spring Boot Microservices
- Service Discovery
- Event-Driven Architecture
- Kafka Messaging
- Docker,Kubernetes
- REST API Design
- Clean Architecture
- Domain-Driven Design Principles
- OpenAPI First Development

---
