# MindScribe

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2021.0.8-blue.svg)](https://spring.io/projects/spring-cloud)
[![FastAPI](https://img.shields.io/badge/FastAPI-Python%203.12-009688.svg)](https://fastapi.tiangolo.com/)
[![HuggingFace](https://img.shields.io/badge/HuggingFace-DistilRoBERTa-yellow.svg)](https://huggingface.co/)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-KRaft%203.7.0-red.svg)](https://kafka.apache.org/)
[![Redis](https://img.shields.io/badge/Redis-Cache%20%26%20Rate%20Limiter-DC382D.svg)](https://redis.io/)
[![MongoDB](https://img.shields.io/badge/MongoDB-Database%20Per%20Service-47A248.svg)](https://www.mongodb.com/)

MindScribe is a polyglot microservices platform built for secure journaling and automated sentiment analysis. It features an event-driven architecture using Apache Kafka, centralized routing with Spring Cloud Gateway, distributed caching and rate limiting with Redis, and AI-based sentiment processing powered by Python and Hugging Face.

---

## Architecture Overview

The system consists of seven microservices operating across Java (Spring Boot) and Python (FastAPI):

| Port | Service | Tech Stack | Role |
| :--- | :--- | :--- | :--- |
| `8761` | **Eureka Server** | Spring Cloud Netflix Eureka | Service registration and discovery |
| `8080` | **API Gateway** | Spring Cloud Gateway, Redis | Gateway routing, JWT verification, token-bucket rate limiting |
| `8081` | **Auth Service** | Spring Boot, Spring Security, MongoDB | User registration, authentication, JWT issuing, Google OAuth2 |
| `8082` | **Journal Service** | Spring Boot, MongoDB, Redis, Resilience4j | Journal entry CRUD operations, weather caching, scheduled tasks |
| `8083` | **Sentiment Service** | Python 3.12, FastAPI, DistilRoBERTa | Real-time sentiment analysis worker |
| `8084` | **Notification Service** | Spring Boot, Apache Kafka, JavaMailSender | Consumes weekly digest topics and sends summary emails |
| `8085` | **Audit Service** | Spring Boot, Apache Kafka, MongoDB | Event-sourced audit store tracking platform mutations |

---

## Core Capabilities

- **Authentication & Security**: Stateless JWT authentication, role-based access control (RBAC), and Google OAuth 2.0 integration.
- **AI Sentiment Analysis**: Asynchronous emotion and sentiment classification using Hugging Face DistilRoBERTa (`sentiment-service`).
- **Event-Driven Messaging**: Asynchronous event streaming via Apache Kafka (KRaft mode) for audit events and weekly sentiment digests.
- **Audit Logging**: Dedicated `audit-service` storing immutable audit logs in MongoDB.
- **Gateway Routing & Rate Limiting**: Centralized API routing, CORS handling, and Redis-backed sliding window rate limiting.
- **Resilience & Circuit Breaking**: Resilience4j circuit breakers protecting inter-service communication and external API integrations.
- **Service Discovery**: Netflix Eureka for client-side load balancing and dynamic service registration.

---

## Getting Started

### Prerequisites

- **Java 8+** (JDK)
- **Python 3.12+** (with `uv` or `pip`)
- **Maven 3.8+**
- **Apache Kafka** (KRaft mode) & **Redis**
- **MongoDB** (Local instance or MongoDB Atlas)

### Running Services via Automation Script

Launch all infrastructure components and microservices with the provided automation script:

```powershell
.\run-services.ps1
```

### Running Services Manually

```bash
# 1. Build parent Maven project
mvn clean install -DskipTests

# 2. Start Service Registry (Eureka Server)
mvn spring-boot:run -pl eureka-server

# 3. Start Python Sentiment Analysis Service
cd sentiment-service
uv run uvicorn app.main:app --port 8083 --host 0.0.0.0

# 4. Start Java Microservices
mvn spring-boot:run -pl api-gateway
mvn spring-boot:run -pl auth-service
mvn spring-boot:run -pl journal-service
mvn spring-boot:run -pl notification-service
mvn spring-boot:run -pl audit-service
```

---

## License

Distributed under the MIT License.
