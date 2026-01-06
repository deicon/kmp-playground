# KMP Playground - Todo List Application

A full-stack Todo list application built with Kotlin Multiplatform, featuring time tracking and multi-project management.

## Features

- **Customer Management**: Organize work by customers
- **Project Management**: Create projects per customer
- **Todo Management**: Track tasks with time estimates and actual time
- **Time Tracking**: Support for flexible time input (2h, 1.5d, 1w)
- **WASM Frontend**: Modern web UI using Compose Multiplatform
- **PostgreSQL**: Reliable data persistence

## Tech Stack

- **Kotlin**: 2.3.0
- **Compose Multiplatform**: 1.7.1 (WASM)
- **Ktor**: 3.0.1 (Backend + Client)
- **SQLDelight**: 2.0.2
- **PostgreSQL**: 15+
- **Docker**: For containerization
- **Playwright**: E2E testing

## Quick Start

### 1. Setup

```bash
# Make setup script executable and run
chmod +x setup.sh
./setup.sh

# Verify Gradle setup
./gradlew build
```

### 2. Run with Docker

```bash
# Start all services (backend + frontend + database)
cd docker
docker-compose up
```

### 3. Development

```bash
# Backend only
./gradlew :backend:run

# Frontend (WASM dev server)
./gradlew :frontend:wasmJsRun

# Run tests
./gradlew test
```

## Project Structure

```
kmp_playground/
├── shared/           # Shared Kotlin code (models, utils)
├── backend/          # Ktor backend server
├── frontend/         # Compose Multiplatform WASM frontend
├── docker/           # Docker Compose configuration
├── e2e-tests/        # Playwright E2E tests
└── scripts/          # Utility scripts
```

## Time Format

The app supports flexible time input:
- `2h` = 2 hours
- `2.5h` = 2.5 hours
- `1d` = 8 hours (1 workday)
- `1.5d` = 12 hours
- `1w` = 40 hours (1 work week)

## Development Workflow

See [PROJECT_PLAN.md](PROJECT_PLAN.md) for the complete implementation plan and progress tracking.

### Current Status

✅ Phase 1: Foundation (In Progress)
- Gradle multi-project setup
- Shared module with domain models
- Time utility functions

🔜 Phase 2: Backend Development
🔜 Phase 3: Frontend Development
🔜 Phase 4: Docker Setup
🔜 Phase 5: E2E Testing

## Prerequisites

- JDK 17 or higher
- Docker & Docker Compose
- Node.js 20+ (for Playwright)

## License

MIT

## Documentation

- [Project Plan](PROJECT_PLAN.md) - Complete implementation roadmap
- [API Documentation](docs/API.md) - Coming soon
- [Architecture](docs/ARCHITECTURE.md) - Coming soon
