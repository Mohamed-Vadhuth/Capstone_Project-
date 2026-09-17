# Multi-stage Dockerfile for Freelancer Platform Spring Boot Backend (Java 21)

# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy Maven wrapper and pom.xml first for dependency caching
COPY mvnw ./
COPY .mvn/ .mvn/
COPY pom.xml ./

# Ensure wrapper script is executable and download dependencies
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy project source and build application package
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create a dedicated non-root user for production execution
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy executable jar from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Cloud providers inject PORT environment variable (e.g. 10000 on Render, 8080 default)
ENV PORT=8080
EXPOSE ${PORT}

# Launch Spring Boot with container memory optimization and dynamic port binding
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -jar app.jar"]
