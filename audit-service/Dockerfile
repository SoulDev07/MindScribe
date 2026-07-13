# ============ Stage 1: Build ============
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy root pom and module pom for dependency caching
COPY pom.xml ./pom.xml
COPY audit-service/pom.xml ./audit-service/pom.xml
RUN mvn -B -pl audit-service -am dependency:go-offline

# Copy module source and package
COPY audit-service/src ./audit-service/src
RUN mvn -B -pl audit-service clean package -DskipTests

# ============ Stage 2: Runtime ============
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN apk add --no-cache curl

COPY --from=builder /app/audit-service/target/*.jar app.jar

EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
