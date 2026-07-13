# ============ Stage 1: Build ============
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy root pom and module pom for dependency caching
COPY pom.xml ./pom.xml
COPY notification-service/pom.xml ./notification-service/pom.xml
RUN mvn -B -pl notification-service -am dependency:go-offline

# Copy module source and package
COPY notification-service/src ./notification-service/src
RUN mvn -B -pl notification-service clean package -DskipTests

# ============ Stage 2: Runtime ============
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN apk add --no-cache curl

COPY --from=builder /app/notification-service/target/*.jar app.jar

EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
