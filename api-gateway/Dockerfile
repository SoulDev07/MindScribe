# ============ Stage 1: Build ============
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy root pom and module pom for dependency caching
COPY pom.xml ./pom.xml
COPY api-gateway/pom.xml ./api-gateway/pom.xml
RUN mvn -B -pl api-gateway -am dependency:go-offline

# Copy module source and package
COPY api-gateway/src ./api-gateway/src
RUN mvn -B -pl api-gateway clean package -DskipTests

# ============ Stage 2: Runtime ============
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN apk add --no-cache curl

COPY --from=builder /app/api-gateway/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
