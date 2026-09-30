# syntax=docker/dockerfile:1
# -----------------------------------------------------------------------------
# Stage 1: Build Spring Boot JAR
# -----------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build production jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# -----------------------------------------------------------------------------
# Stage 2: Minimal Production JRE Runtime
# -----------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runner

RUN addgroup -S hrms && adduser -S hrms -G hrms

WORKDIR /app

COPY --from=builder /build/target/hrms.jar /app/app.jar

USER hrms:hrms

ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0"

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
