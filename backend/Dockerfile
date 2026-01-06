# Multi-stage Dockerfile for Ktor backend

# Stage 1: Build
FROM gradle:8.5-jdk17 AS build

WORKDIR /app

# Copy gradle files
COPY gradle gradle
COPY gradlew .
COPY gradlew.bat .
COPY settings.gradle.kts .
COPY build.gradle.kts .
COPY gradle.properties .

# Copy shared module
COPY shared shared/

# Copy backend module
COPY backend backend/

# Build the application with Shadow JAR
RUN ./gradlew :backend:shadowJar --no-daemon

# Stage 2: Runtime
FROM amazoncorretto:17-alpine

WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/backend/build/libs/*-all.jar app.jar

# Expose port
EXPOSE 8080

# Set environment variables with defaults
ENV PORT=8080
ENV DB_HOST=postgres
ENV DB_PORT=5432
ENV DB_NAME=todos
ENV DB_USER=postgres
ENV DB_PASSWORD=postgres
ENV DB_MAX_POOL_SIZE=10

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
