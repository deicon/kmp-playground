# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A full-stack Todo list application built with **Kotlin Multiplatform (KMP)**, featuring time tracking and multi-project management. The application allows organizing work by customers, creating projects per customer, and tracking tasks with time estimates and actual time spent.

**Key Technologies:**
- Kotlin 2.3.0 with Kotlin Multiplatform
- Compose Multiplatform 1.7.1 (WASM target for web UI)
- Ktor 3.0.1 (Backend server + HTTP client)
- SQLDelight 2.0.2 (Type-safe SQL)
- PostgreSQL 15+
- Docker & Docker Compose

## Project Structure

This is a Gradle multi-project build with three main modules:

```
kmp_playground/
├── shared/          # Shared KMP code (domain models, DTOs, utilities)
│   ├── commonMain/  # Platform-agnostic business logic
│   ├── commonTest/  # Shared tests
│   ├── jvmMain/     # JVM-specific code
│   └── wasmJsMain/  # WASM-specific code
├── backend/         # Ktor backend server (JVM)
│   ├── main/        # Backend application code
│   └── test/        # Backend unit tests
├── frontend/        # Compose Multiplatform WASM frontend
│   ├── commonMain/  # Common UI code
│   └── wasmJsMain/  # WASM-specific entry point
├── docker/          # Docker Compose configuration
├── e2e-tests/       # Playwright E2E tests
└── scripts/         # Utility scripts
```

## Common Commands

### Development

```bash
# Build entire project
./gradlew build

# Build specific module
./gradlew :shared:build
./gradlew :backend:build
./gradlew :frontend:build

# Run backend server (starts on default Ktor port)
./gradlew :backend:run

# Run frontend WASM dev server
./gradlew :frontend:wasmJsRun

# Run all tests
./gradlew test

# Run tests for specific module
./gradlew :shared:test
./gradlew :backend:test

# Clean build artifacts
./gradlew clean
```

### Docker

```bash
# Start all services (backend + frontend + PostgreSQL)
cd docker
docker-compose up

# Start in detached mode
docker-compose up -d

# Stop all services
docker-compose down

# View logs
docker-compose logs -f
```

### Initial Setup

```bash
# Make setup script executable and run
chmod +x setup.sh
./setup.sh

# Verify setup
./gradlew build
```

## Architecture

### Domain Model

The application follows a hierarchical data model:

**Customer** → **Project** → **Todo**

Each entity contains:
- `Customer`: id (UUID), name, email (optional), createdAt
- `Project`: id (UUID), customerId (FK), name, description (optional), createdAt
- `Todo`: id (UUID), projectId (FK), title, description, dueDate, estimatedHours, actualHours, completed, createdAt, completedAt

### Time Tracking System

**Time Storage:** All time values are stored as `Double` representing decimal hours in the database.

**Time Input Formats:** The application supports flexible time input:
- `2h` or `2.5h` = hours (stored as-is: 2.0 or 2.5)
- `1d` or `1.5d` = workdays (converted: 1d = 8.0 hours, 1.5d = 12.0 hours)
- `1w` = work weeks (converted: 1w = 40.0 hours, assuming 5-day work week)

**Implementation:** Time parsing and conversion utilities are in the `shared` module for use across all platforms.

### Module Responsibilities

**shared/**: Contains all platform-agnostic code that is shared between backend and frontend:
- Domain models (Customer, Project, Todo)
- DTOs for API requests/responses
- Time utility functions (parsing "2h", "1d", "1w" formats)
- Validation logic
- Common interfaces

**backend/**: Ktor server application:
- REST API endpoints (CRUD operations for Customer, Project, Todo)
- SQLDelight database layer with PostgreSQL
- Repository pattern for data access
- Request/response handling with kotlinx.serialization
- Database schema definitions (.sq files for SQLDelight)

**frontend/**: Compose Multiplatform web UI (WASM target):
- Compose UI components and screens
- State management with ViewModels
- Ktor client for API communication
- Navigation between Customer → Project → Todo screens
- Time input/display components with format conversion

## Database

**Type**: PostgreSQL 15+
**ORM**: SQLDelight 2.0.2 (type-safe SQL queries)

**Schema files**: Located in `backend/src/main/sqldelight/` (standard SQLDelight location)

**Foreign Key Relationships**:
- `Todo.projectId` → `Project.id`
- `Project.customerId` → `Customer.id`

**Important Indexes** (should be created for performance):
- Todos by project
- Projects by customer
- Todos by due date

## Key Dependencies & Versions

Versions are centralized in `gradle.properties`:
- Kotlin: 2.3.0
- Compose Multiplatform: 1.7.1
- Ktor: 3.0.1
- SQLDelight: 2.0.2
- Koin: 4.0.0 (dependency injection)
- kotlinx.serialization: 1.7.3
- kotlinx.coroutines: 1.9.0
- kotlinx.datetime: 0.6.1 (cross-platform date/time)
- PostgreSQL Driver: 42.7.4
- JUnit: 5.11.3
- Kotest: 5.9.1

## Development Guidelines

### Working with Shared Module

- All domain models and DTOs **must** be defined in `shared/commonMain`
- Use `kotlinx.serialization` for JSON serialization (both backend and frontend use this)
- Use `kotlinx.datetime` for date/time handling (cross-platform compatible)
- Platform-specific code goes in `jvmMain` (backend) or `wasmJsMain` (frontend)

### Backend Development

- Use SQLDelight for all database operations (type-safe, generates Kotlin code from SQL)
- API endpoints should use DTOs from the `shared` module
- Configure CORS to allow frontend access during development
- Use Ktor's content negotiation for JSON serialization/deserialization
- Repository pattern: Create interfaces in `shared`, implement in `backend`

### Frontend Development

- Target: `wasmJs` (Compose Multiplatform for web)
- Use Ktor client for HTTP requests (shares serialization with backend)
- Compose UI follows declarative pattern with state management
- Time input components should support "2h", "1.5d", "1w" formats and convert to decimal hours
- Navigation structure: Customer List → Project List (filtered by customer) → Todo List (filtered by project)

### Testing

- **Unit tests**: Test individual functions and components (JUnit/Kotest)
- **Integration tests**: Test API endpoints with test database (Ktor test client)
- **E2E tests**: Full user flows with Playwright (in `e2e-tests/`)

### WASM-Specific Considerations

- WASM target is stable as of Kotlin 2.0+, but some libraries may not be fully compatible
- Use WASM-compatible dependencies only in frontend
- WASM module requires proper MIME types (`application/wasm`) when served via nginx
- Development server: `./gradlew :frontend:wasmJsRun`

## Current Implementation Status

**Phase 1 - Foundation**: ~60% Complete
- ✅ Gradle multi-project setup
- ✅ Shared module structure with source sets
- ✅ Version catalog in gradle.properties
- ✅ Documentation (README, PROJECT_PLAN, PROGRESS)
- ⏳ Domain models implementation (planned)
- ⏳ Time utility functions (planned)
- ⏳ SQLDelight schema files (planned)

**Phase 2 - Backend**: Not started
**Phase 3 - Frontend**: Not started
**Phase 4 - Docker**: Directory structure exists
**Phase 5 - E2E Testing**: Directory structure exists

See `PROJECT_PLAN.md` for complete implementation roadmap and detailed task breakdown.

## Important Files

- `settings.gradle.kts` - Defines included modules (`:shared`, `:backend`, `:frontend`)
- `build.gradle.kts` - Root build configuration with plugin versions
- `gradle.properties` - All dependency versions and Gradle settings
- `PROJECT_PLAN.md` - Complete implementation plan with phases and tasks
- `README.md` - Project overview and quick start guide

## Technical Decisions

Key architectural decisions (from PROJECT_PLAN.md):

1. **SQLDelight over Exposed**: Type-safe SQL, better KMP support, WASM-compatible
2. **Time as Decimal Hours**: Simple storage (Double), flexible input formats (h/d/w), easy aggregation
3. **WASM Target**: Modern web deployment, shares UI code with potential future Android/iOS apps
4. **Compose Multiplatform**: Single UI codebase across web, Android, desktop
5. **Ktor Full-Stack**: Same framework for backend server and frontend HTTP client, simplified serialization

## Next Steps for New Contributors

1. Read `PROJECT_PLAN.md` to understand the complete implementation roadmap
2. Run `./setup.sh` to initialize project structure (if not done)
3. Verify build works: `./gradlew build`
4. Start with Phase 1 tasks (shared module implementation) or Phase 2 (backend development)
5. Follow the architectural patterns established in `PROJECT_PLAN.md`
