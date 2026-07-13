# ============ Stage 1: Build ============
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy root pom and module pom for dependency caching
COPY pom.xml ./pom.xml
COPY eureka-server/pom.xml ./eureka-server/pom.xml
RUN mvn -B -pl eureka-server -am dependency:go-offline

# Copy module source and package
COPY eureka-server/src ./eureka-server/src
RUN mvn -B -pl eureka-server clean package -DskipTests

# ============ Stage 2: Runtime ============
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN apk add --no-cache curl

COPY --from=builder /app/eureka-server/target/*.jar app.jar

EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
