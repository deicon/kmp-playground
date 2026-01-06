# Multi-stage Dockerfile for Compose Multiplatform WASM frontend

# Stage 1: Build
FROM gradle:8.5-jdk17 AS build

WORKDIR /app

# Install libatomic for Node.js on ARM64
RUN apt-get update && apt-get install -y libatomic1 && rm -rf /var/lib/apt/lists/*

# Copy gradle files
COPY gradle gradle
COPY gradlew .
COPY gradlew.bat .
COPY settings.gradle.kts .
COPY build.gradle.kts .
COPY gradle.properties .

# Copy shared module
COPY shared shared/

# Copy frontend module
COPY frontend frontend/

# Build the WASM application
RUN ./gradlew :frontend:wasmJsBrowserDistribution --no-daemon

# Stage 2: Runtime (nginx)
FROM nginx:alpine

WORKDIR /usr/share/nginx/html

# Remove default nginx files
RUN rm -rf ./*

# Copy built WASM bundle from build stage
COPY --from=build /app/frontend/build/dist/wasmJs/productionExecutable/ .

# Copy nginx configuration
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf

# Expose port
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:80/ || exit 1

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
