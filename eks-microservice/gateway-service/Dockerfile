# ---- Stage 1: Build ----
FROM maven:3.9.5-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom first (layer caching - faster rebuilds)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests

# ---- Stage 2: Run ----
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Security: run as non-root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy jar from builder
COPY --from=builder /app/target/gateway-service.jar app.jar

EXPOSE 8080

# Health check built into Docker too
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget -qO- http://localhost:8080/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
