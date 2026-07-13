# Microservices E-Commerce Backend

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
## Technologies Used
- Java 17
- Spring Boot 4
- Spring Cloud Netflix Eureka
- Spring Data JPA
- PostgreSQL
- Apache Kafka
- Docker & Docker Compose
- OpenAPI Generator
- SpringDoc OpenAPI (Swagger)
- MapStruct
- Lombok
- Maven

---

## Microservices

### Customer Service

Responsible for:

- Creating customers
- Retrieving customer information
- Managing customer addresses

### Order Service

Responsible for:

- Creating orders
- Validating customer information
- Processing order items
- Publishing order events to Kafka
- Idempotent order creation

### Notification Service

Responsible for:

- Consuming Kafka order events
- Processing asynchronous notifications

---

### Eureka Server

Responsible for:

- Service registration
- Service discovery

## Features

- Microservices Architecture
- Service Discovery using Eureka
- REST APIs
- Dockerized Services
- PostgreSQL Persistence
- Kafka Event Publishing
- Kafka Consumer
- OpenAPI Generated APIs
- Clean Architecture
- DTO Mapping using MapStruct
- Validation using Jakarta Validation
- Idempotent Order Creation

---

## Running the Project

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

## Example OF API Requests ID GIVEN INSIDE EACH MICROSERVICE(Please check README file inside each service to run each service independently)
---

## Project Structure

```
Microservices/
│
├── customer-service/
├── order-service/
├── notification-service/
├── eureka-server/
├── docker-compose.yml
└── README.md
```

---

## Future Improvements

- API Gateway
- Centralized Configuration Server
- JWT Authentication
- Distributed Tracing
- Circuit Breaker (Resilience4j)
- Monitoring with Prometheus & Grafana
- CI/CD Pipeline
- Kubernetes Deployment

---
## Learning Objectives
This project was built to practice:

- Spring Boot Microservices
- Service Discovery
- Event-Driven Architecture
- Kafka Messaging
- Docker
- REST API Design
- Clean Architecture
- Domain-Driven Design Principles
- OpenAPI First Development

---
