# MindScribe

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2021.0.8-blue.svg)](https://spring.io/projects/spring-cloud)
[![FastAPI](https://img.shields.io/badge/FastAPI-Python%203.12-009688.svg)](https://fastapi.tiangolo.com/)
[![HuggingFace](https://img.shields.io/badge/HuggingFace-DistilRoBERTa-yellow.svg)](https://huggingface.co/)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-KRaft%203.7.0-red.svg)](https://kafka.apache.org/)
[![Redis](https://img.shields.io/badge/Redis-Cache%20%26%20Rate%20Limiter-DC382D.svg)](https://redis.io/)
[![MongoDB](https://img.shields.io/badge/MongoDB-Database%20Per%20Service-47A248.svg)](https://www.mongodb.com/)

A distributed polyglot microservices platform for personal journaling. It features asynchronous AI sentiment analysis with Hugging Face DistilRoBERTa, event-driven notifications over Apache Kafka, token-bucket rate limiting via Redis, and an immutable audit log store.

---

## Services

| Service | Port | Tech Stack | Role |
| :--- | :--- | :--- | :--- |
| `eureka-server` | 8761 | Spring Cloud Eureka | Service registration and discovery |
| `api-gateway` | 8080 | Spring Cloud Gateway, Redis | Gateway routing, JWT validation, Redis rate limiting |
| `auth-service` | 8081 | Spring Boot, MongoDB | User lifecycle, password hashing, JWT issuing |
| `journal-service` | 8082 | Spring Boot, MongoDB, Redis | Journal entry CRUD, weather caching, weekly scheduler |
| `sentiment-service` | 8083 | Python 3.12, FastAPI, DistilRoBERTa | NLP classification (`POSITIVE`, `NEGATIVE`, `NEUTRAL`) |
| `notification-service` | 8084 | Spring Boot, Kafka, JavaMail | Consumes sentiment summaries and dispatches digest emails |
| `audit-service` | 8085 | Spring Boot, Kafka, MongoDB | Event-sourced mutation tracking across services |

---

## Kafka Event Pipelines

| Topic | Producer | Consumer | Payload / Purpose |
| :--- | :--- | :--- | :--- |
| `journal-entries` | `journal-service` | `journal-service` & `sentiment-service` | Entry creation event for sentiment inference |
| `weekly-sentiments` | `journal-service` | `notification-service` | Aggregated 7-day user sentiment digest for email dispatch |
| `audit-log` | `auth-service`, `journal-service` | `audit-service` | Structured audit events (`ENTRY_CREATED`, `ENTRY_DELETED`, etc.) |

---

## Getting Started

### Prerequisites

- Java JDK 8+
- Maven 3.8+
- Python 3.12+
- Docker (for Kafka KRaft container)
- Redis & MongoDB (local instances or cloud connections)

### Automated Launch (PowerShell)

The root script starts Kafka in Docker, checks local Redis/MongoDB, and boots all 7 microservices:

```powershell
.\run-services.ps1
```

### Launch with Docker Compose

Build and spin up the complete polyglot microservices stack and all infrastructure with a single command:

```bash
docker compose up --build
```

### Manual Launch

1. **Start Infrastructure**:
   ```bash
   # Kafka (KRaft mode, no ZooKeeper needed)
   docker run -d --name kafka -p 9092:9092 \
     -e KAFKA_NODE_ID=1 \
     -e KAFKA_PROCESS_ROLES=broker,controller \
     -e KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093 \
     -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
     -e KAFKA_CONTROLLER_QUORUM_VOTERS=1@localhost:9093 \
     -e KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER \
     -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT \
     -e CLUSTER_ID=MkU3OEVBNTcwNTJENDM2Qk \
     apache/kafka:3.7.0
   ```

2. **Build Maven Modules**:
   ```bash
   mvn clean install -DskipTests
   ```

3. **Start Microservices (in order)**:
   ```bash
   # Service Discovery
   mvn spring-boot:run -pl eureka-server

   # Python Sentiment Service
   cd sentiment-service
   uv run uvicorn app.main:app --port 8083 --host 0.0.0.0
   cd ..

   # API Gateway
   mvn spring-boot:run -pl api-gateway

   # Core Services
   mvn spring-boot:run -pl auth-service
   mvn spring-boot:run -pl journal-service
   mvn spring-boot:run -pl notification-service
   mvn spring-boot:run -pl audit-service
   ```

---

## Testing

```bash
# Run Java unit & slice tests across all modules
mvn test-compile

# Run Python Sentiment Service tests
cd sentiment-service
uv run pytest
```

---

## License

[MIT](LICENSE.md)
