# MindScribe

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2021.0.8-blue.svg)](https://spring.io/projects/spring-cloud)
[![FastAPI](https://img.shields.io/badge/FastAPI-Python%203.12-009688.svg)](https://fastapi.tiangolo.com/)
[![HuggingFace](https://img.shields.io/badge/HuggingFace-DistilRoBERTa-yellow.svg)](https://huggingface.co/)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-KRaft%203.7.0-red.svg)](https://kafka.apache.org/)
[![Redis](https://img.shields.io/badge/Redis-Cache%20%26%20Rate%20Limiter-DC382D.svg)](https://redis.io/)
[![MongoDB](https://img.shields.io/badge/MongoDB-Database%20Per%20Service-47A248.svg)](https://www.mongodb.com/)

A polyglot microservices platform for secure journaling, event-driven processing, and AI-powered sentiment analysis.

---

## Services Architecture

| Service | Port | Tech Stack | Description |
| :--- | :--- | :--- | :--- |
| `eureka-server` | 8761 | Spring Cloud Eureka | Service discovery and registry |
| `api-gateway` | 8080 | Spring Cloud Gateway, Redis | Edge routing, JWT validation & token-bucket rate limiting |
| `auth-service` | 8081 | Spring Boot, MongoDB | Identity management, JWT issuing & Google OAuth2 |
| `journal-service` | 8082 | Spring Boot, MongoDB, Redis | Core journal CRUD, weather integration & Resilience4j fault tolerance |
| `sentiment-service` | 8083 | Python 3.12, FastAPI, DistilRoBERTa | Real-time NLP sentiment analysis worker |
| `notification-service` | 8084 | Spring Boot, Kafka, JavaMail | Asynchronous email dispatch for weekly digests |
| `audit-service` | 8085 | Spring Boot, Kafka, MongoDB | Immutable event-sourced audit logger |

---

## Technical Highlights

- **AI Sentiment Analysis**: Asynchronous emotion and sentiment classification using Hugging Face DistilRoBERTa (`sentiment-service`).
- **Event-Driven Messaging**: Asynchronous inter-service communication via Apache Kafka (KRaft mode).
- **Audit Logging**: Dedicated `audit-service` storing immutable audit logs in MongoDB.
- **Fault Tolerance**: Resilience4j circuit breakers and fallbacks protecting external API and inter-service calls.
- **Distributed Caching & Rate Limiting**: Redis-backed sliding window rate limiting at the gateway and response caching in services.

---

## Getting Started

### Prerequisites

- Java 8+
- Python 3.12+ (with `uv` or `pip`)
- Maven 3.8+
- Apache Kafka (KRaft mode) & Redis
- MongoDB

### Quickstart

Run all infrastructure components and microservices with the PowerShell script:

```powershell
.\run-services.ps1
```

Or start services manually:

```bash
# 1. Build project
mvn clean install -DskipTests

# 2. Start Service Registry
mvn spring-boot:run -pl eureka-server

# 3. Start Sentiment Service
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

Distributed under the [MIT License](LICENSE.md).
