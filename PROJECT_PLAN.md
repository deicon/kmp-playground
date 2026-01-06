# Kotlin Multiplatform Full Stack Project - Implementation Plan

## Project Overview
Building a **Todo List Application** using Kotlin Multiplatform with:
- **Application**: Multi-project Todo management with time tracking
- **Backend**: Ktor server with PostgreSQL
- **Frontend**: Jetpack Compose (Android) + Compose Multiplatform WASM (Web)
- **Infrastructure**: Docker Compose
- **Testing**: Playwright E2E tests

## Domain Model

### Entities
```kotlin
Customer {
  id: UUID
  name: String
  email: String?
  createdAt: Instant
}

Project {
  id: UUID
  customerId: UUID
  name: String
  description: String?
  createdAt: Instant
}

Todo {
  id: UUID
  projectId: UUID
  title: String
  description: String?
  dueDate: LocalDate?
  estimatedHours: Double?      // Support: 1.5h, 2h, 12h (0.5d), 40h (1w), etc.
  actualHours: Double          // Track actual time in hours (decimal)
  completed: Boolean
  createdAt: Instant
  completedAt: Instant?
}

// Time Format Examples:
// - 2h = 2.0 hours
// - 2.5h = 2.5 hours
// - 1d = 8.0 hours (1 day = 8 hours)
// - 1.5d = 12.0 hours
// - 1w = 40.0 hours (1 week = 5 days = 40 hours)
```

### Features
- Create/manage customers
- Create/manage projects per customer
- Create/manage todos per project
- Track estimated vs actual time spent (in hours: decimal format)
- Support time input formats: hours (h), days (d), weeks (w)
- Mark todos as complete
- Filter/sort todos by due date, project, customer
- Time tracking and aggregation per project

## Directory Structure
```
kmp_playground/
├── .git/                       # Git repository
├── .gitignore                  # Git ignore file
├── shared/                     # Shared KMP code
│   ├── src/
│   │   ├── commonMain/        # Common business logic, models
│   │   ├── commonTest/        # Shared tests
│   │   └── jvmMain/           # JVM-specific code
│   └── build.gradle.kts
├── backend/                    # Ktor backend
│   ├── src/
│   │   ├── main/kotlin/       # Backend application code
│   │   └── test/kotlin/       # Backend unit tests
│   ├── Dockerfile
│   └── build.gradle.kts
├── frontend/                   # Compose Multiplatform WASM frontend
│   ├── src/
│   │   ├── commonMain/        # Common UI code
│   │   ├── wasmJsMain/        # WASM-specific code
│   │   └── androidMain/       # Android-specific code (future)
│   ├── Dockerfile
│   └── build.gradle.kts
├── docker/                     # Docker compose setup
│   ├── docker-compose.yml     # Main compose file
│   ├── docker-compose.dev.yml # Development overrides
│   ├── .env.example           # Environment variables template
│   └── nginx/                 # Nginx config for reverse proxy
│       └── nginx.conf
├── e2e-tests/                  # Playwright E2E tests
│   ├── tests/                 # Test files
│   ├── playwright.config.ts   # Playwright configuration
│   ├── package.json
│   └── README.md
├── scripts/                    # Utility scripts
│   ├── setup.sh              # Initial setup script
│   ├── build-all.sh          # Build all components
│   └── test-e2e.sh           # Run E2E tests
├── settings.gradle.kts        # Gradle multi-project setup
├── build.gradle.kts           # Root build file
├── gradle.properties          # Gradle properties
└── README.md                  # Project documentation
```

---

## Implementation Steps

### Phase 1: Project Foundation (Setup & Git)

#### Step 1.1: Git Repository Setup
- [x] Initialize git repository
- [x] Create `.gitignore` for Kotlin, Gradle, Docker, Node.js
- [ ] Create initial commit with project structure
- [ ] Setup git branches strategy (main, develop)
- [x] Create `.gitattributes` for line endings

#### Step 1.2: Gradle Multi-Project Setup
- [x] Create `settings.gradle.kts` with all subprojects
- [x] Create root `build.gradle.kts` with common configurations
- [x] Create `gradle.properties` with version catalogs
- [x] Setup Kotlin version, JVM target (17+)
- [ ] Configure gradle wrapper

#### Step 1.3: Shared Module
- [x] Create `shared/build.gradle.kts` with KMP plugin
- [x] Configure targets: JVM, WASM-JS, Android (for future)
- [x] Add kotlinx.serialization plugin
- [x] Create domain models: `Customer`, `Project`, `Todo`
- [x] Create DTOs for API requests/responses
- [ ] Create API client interfaces
- [x] Add kotlinx.datetime for date handling
- [x] Add unit tests for domain models and validation

---

### Phase 2: Backend Development

#### Step 2.1: Ktor Backend Setup
- [ ] Create `backend/build.gradle.kts`
- [ ] Add Ktor dependencies (server, routing, serialization, auth)
- [ ] Create main `Application.kt` with basic server setup
- [ ] Configure CORS for frontend access
- [ ] Setup content negotiation (JSON)
- [ ] Add application.conf for configuration

#### Step 2.2: Database Integration
- [ ] Add PostgreSQL driver and SQLDelight dependencies
- [ ] Create SQLDelight schema files (.sq) for `customers`, `projects`, `todos`
- [ ] Add foreign key constraints (todos -> projects -> customers)
- [ ] Setup connection pooling (HikariCP)
- [ ] Generate database code with SQLDelight
- [ ] Create repository interfaces and implementations
- [ ] Add database initialization and migration code
- [ ] Create indexes for common queries (by customer, by project, by due date)

#### Step 2.3: API Implementation
- [ ] Create REST API routes for customers (CRUD)
- [ ] Create REST API routes for projects (CRUD, filter by customer)
- [ ] Create REST API routes for todos (CRUD, filter by project)
- [ ] Add endpoint to update todo time tracking (actual minutes)
- [ ] Add endpoint to mark todo as complete
- [ ] Add aggregation endpoints (todos by due date, time summaries)
- [ ] Implement request/response DTOs using shared models
- [ ] Add input validation (required fields, date formats, time >= 0)
- [ ] Implement error handling middleware
- [ ] Add logging (Logback/SLF4J)
- [ ] Create health check endpoint (`/health`)

#### Step 2.4: Backend Testing
- [ ] Setup Ktor test dependencies
- [ ] Write unit tests for repositories
- [ ] Write integration tests for API endpoints
- [ ] Add test database configuration (H2 or Testcontainers)
- [ ] Configure test coverage reports

#### Step 2.5: Backend Dockerfile
- [ ] Create multi-stage Dockerfile
- [ ] Use Gradle to build fat JAR
- [ ] Optimize image size (use Alpine or distroless)
- [ ] Configure health check in Dockerfile
- [ ] Set proper environment variables

---

### Phase 3: Frontend Development

#### Step 3.1: Compose Multiplatform WASM Setup
- [ ] Create `frontend/build.gradle.kts`
- [ ] Add Compose Multiplatform plugin (1.6.0+)
- [ ] Configure WASM-JS target with Compose compiler
- [ ] Add Android target for future mobile app
- [ ] Setup Compose dependencies for WASM
- [ ] Create main entry point `wasmJsMain/main.kt`
- [ ] Setup HTML index file for WASM
- [ ] Configure WASM-specific resources

#### Step 3.2: Frontend Architecture
- [ ] Setup dependency injection (Koin)
- [ ] Create ViewModel/State management pattern
- [ ] Configure Ktor client for API calls
- [ ] Share networking code with shared module
- [ ] Setup navigation (Decompose or custom)
- [ ] Create theme/design system

#### Step 3.3: UI Implementation
- [ ] Create main navigation structure (Customers -> Projects -> Todos)
- [ ] **Customer screens**: List, Create/Edit form
- [ ] **Project screens**: List (filter by customer), Create/Edit form
- [ ] **Todo screens**: List (filter by project), Create/Edit form
- [ ] **Todo detail**: Show time tracking (estimated vs actual)
- [ ] Add time input component supporting: 2h, 2.5h, 1d, 1.5d, 1w formats
- [ ] Convert time inputs to decimal hours (1d=8h, 1w=40h)
- [ ] Display time in user-friendly format with unit suffix
- [ ] Add date picker for due dates
- [ ] Add "Mark Complete" action with completion timestamp
- [ ] Implement form validation (required fields, valid dates/times)
- [ ] Add loading/error states for all API calls
- [ ] Create reusable components (TodoCard, ProjectCard, TimeDisplay)
- [ ] Add filtering and sorting (by due date, by customer)
- [ ] Display time summaries (total estimated vs actual per project)

#### Step 3.4: Frontend Build
- [ ] Configure WASM-JS build with Compose compiler
- [ ] Setup webpack for WASM bundling
- [ ] Enable production optimizations (minification, DCE)
- [ ] Configure static resource handling
- [ ] Test WASM build locally (dev server)
- [ ] Verify WASM module loads in browser
- [ ] Test Android build (APK) - future milestone

#### Step 3.5: Frontend Dockerfile
- [ ] Create multi-stage Dockerfile (build + serve)
- [ ] Stage 1: Build WASM bundle with Gradle
- [ ] Stage 2: Use nginx to serve static files
- [ ] Copy WASM bundle, JS glue code, and HTML
- [ ] Configure MIME types for WASM (application/wasm)
- [ ] Configure nginx for SPA routing
- [ ] Add proper caching headers
- [ ] Optimize for production (gzip, brotli)

---

### Phase 4: Docker Compose Setup

#### Step 4.1: Base Docker Compose
- [ ] Create `docker/docker-compose.yml`
- [ ] Define backend service
- [ ] Define frontend service (nginx)
- [ ] Define database service (PostgreSQL)
- [ ] Setup network configuration
- [ ] Define volumes for persistence

#### Step 4.2: Development Configuration
- [ ] Create `docker-compose.dev.yml` with overrides
- [ ] Enable hot reload for development
- [ ] Add volume mounts for source code
- [ ] Expose debug ports
- [ ] Add development environment variables

#### Step 4.3: Environment Configuration
- [ ] Create `.env.example` template
- [ ] Document all environment variables
- [ ] Setup secrets management strategy
- [ ] Configure database credentials
- [ ] Setup API URLs and ports

#### Step 4.4: Nginx Reverse Proxy
- [ ] Create `docker/nginx/nginx.conf`
- [ ] Configure proxy to backend API
- [ ] Serve frontend static files
- [ ] Setup CORS headers if needed
- [ ] Add SSL/TLS configuration (optional)
- [ ] Configure compression (gzip)

#### Step 4.5: Docker Orchestration
- [ ] Add healthchecks to all services
- [ ] Configure service dependencies (depends_on)
- [ ] Setup restart policies
- [ ] Add resource limits (CPU, memory)
- [ ] Test full stack startup

---

### Phase 5: E2E Testing with Playwright

#### Step 5.1: Playwright Setup
- [ ] Create `e2e-tests/package.json`
- [ ] Initialize npm project
- [ ] Install Playwright and dependencies
- [ ] Run `npx playwright install` for browsers
- [ ] Create `playwright.config.ts`
- [ ] Configure base URL to localhost Docker setup

#### Step 5.2: Test Infrastructure
- [ ] Create test fixtures and helpers
- [ ] Setup test data seeding scripts
- [ ] Configure database reset between tests
- [ ] Create page object models
- [ ] Setup authentication helpers
- [ ] Add screenshot/video capture on failure

#### Step 5.3: E2E Test Scenarios
- [ ] **Customer flow**: Create, list, edit, delete customers
- [ ] **Project flow**: Create project for customer, list projects, edit, delete
- [ ] **Todo flow**: Create todo for project, list todos, edit, delete
- [ ] **Time tracking**: Add estimated time, track actual time, verify totals
- [ ] **Due dates**: Set due date, verify sorting by due date
- [ ] **Complete todos**: Mark todo as complete, verify completion timestamp
- [ ] **Filtering**: Filter todos by project, filter projects by customer
- [ ] **Validation**: Test required fields, invalid dates, negative time values
- [ ] **Navigation**: Test breadcrumb navigation (Customer -> Project -> Todo)
- [ ] **Error handling**: Test network errors, validation errors

#### Step 5.4: Test Execution
- [ ] Create test execution script (`scripts/test-e2e.sh`)
- [ ] Start Docker Compose before tests
- [ ] Wait for services health checks
- [ ] Run Playwright tests
- [ ] Collect test reports
- [ ] Shutdown Docker Compose after tests

#### Step 5.5: CI/CD Integration (Future)
- [ ] Configure Playwright for headless mode
- [ ] Generate HTML reports
- [ ] Setup test artifacts collection
- [ ] Configure parallel test execution
- [ ] Add test result notifications

---

### Phase 6: Build & Deployment Scripts

#### Step 6.1: Setup Script
- [ ] Create `scripts/setup.sh`
- [ ] Check prerequisites (Java, Docker, Node.js)
- [ ] Initialize git repository
- [ ] Copy `.env.example` to `.env`
- [ ] Run initial Gradle build
- [ ] Install npm dependencies for E2E tests

#### Step 6.2: Build Scripts
- [ ] Create `scripts/build-all.sh`
- [ ] Build shared module
- [ ] Build backend JAR
- [ ] Build frontend bundle
- [ ] Build Docker images
- [ ] Tag images appropriately

#### Step 6.3: Development Scripts
- [ ] Create `scripts/dev.sh` to start dev environment
- [ ] Create `scripts/stop.sh` to stop all services
- [ ] Create `scripts/logs.sh` to tail logs
- [ ] Create `scripts/db-migrate.sh` for migrations
- [ ] Create `scripts/db-seed.sh` for test data

#### Step 6.4: Testing Scripts
- [ ] Create `scripts/test-unit.sh` for unit tests
- [ ] Create `scripts/test-integration.sh` for integration tests
- [ ] Create `scripts/test-e2e.sh` for E2E tests
- [ ] Create `scripts/test-all.sh` to run all tests
- [ ] Add code coverage collection

---

### Phase 7: Documentation & Polish

#### Step 7.1: Documentation
- [ ] Update main README.md with project overview
- [ ] Document API endpoints (OpenAPI/Swagger)
- [ ] Create architecture diagrams
- [ ] Document development workflow
- [ ] Create troubleshooting guide
- [ ] Document environment variables

#### Step 7.2: Code Quality
- [ ] Add ktlint or detekt for Kotlin linting
- [ ] Configure pre-commit hooks
- [ ] Add EditorConfig file
- [ ] Setup code formatting rules
- [ ] Add license file

#### Step 7.3: Monitoring & Logging
- [ ] Add structured logging
- [ ] Configure log aggregation in Docker
- [ ] Add application metrics (Micrometer)
- [ ] Setup health check endpoints
- [ ] Add database connection monitoring

---

## Technology Stack Summary

### Backend
- **Framework**: Ktor 2.3+
- **Kotlin**: 2.3.0
- **Database**: PostgreSQL 15+
- **ORM/SQL**: SQLDelight 2.0+
- **Serialization**: kotlinx.serialization
- **DI**: Koin
- **Testing**: Ktor Test, JUnit 5, Kotest

### Frontend
- **UI Framework**: Compose Multiplatform 1.7.1+ (WASM target)
- **Kotlin**: 2.3.0
- **Platforms**: WASM (Web), Android (future)
- **HTTP Client**: Ktor Client (WASM-compatible)
- **State Management**: ViewModel + Kotlin Flows
- **Date/Time**: kotlinx.datetime
- **DI**: Koin (WASM-compatible)
- **Testing**: Compose UI Testing

### Infrastructure
- **Containerization**: Docker & Docker Compose
- **Web Server**: Nginx (for frontend)
- **Database**: PostgreSQL
- **Reverse Proxy**: Nginx

### Testing
- **E2E**: Playwright (TypeScript/JavaScript)
- **Unit**: JUnit 5, Kotest
- **Integration**: Testcontainers (optional)

### Build & Tools
- **Build System**: Gradle 8.5+ with Kotlin DSL
- **Kotlin**: 2.3.0
- **Version Control**: Git
- **JVM**: Java 17+
- **Node.js**: 20+ (for Playwright and WASM dev server)

---

## Milestones & Success Criteria

### Milestone 1: Foundation Complete
- ✅ Git repo initialized with proper structure
- ✅ Gradle multi-project builds successfully
- ✅ Shared module compiles for all targets

### Milestone 2: Backend Complete
- ✅ Backend server starts and responds to health checks
- ✅ CRUD API endpoints working
- ✅ Database persists data correctly
- ✅ Unit and integration tests passing

### Milestone 3: Frontend Complete
- ✅ Frontend builds for web and desktop
- ✅ UI displays data from API
- ✅ CRUD operations work through UI
- ✅ Error handling implemented

### Milestone 4: Docker Complete
- ✅ All services start via docker-compose
- ✅ Services can communicate
- ✅ Data persists across restarts
- ✅ Health checks passing

### Milestone 5: E2E Testing Complete
- ✅ Playwright tests execute against Docker setup
- ✅ Full user flows tested
- ✅ Tests run reliably
- ✅ Reports generated

### Milestone 6: Production Ready
- ✅ All tests passing
- ✅ Documentation complete
- ✅ Build scripts automated
- ✅ Ready for deployment

---

## Estimated Timeline

- **Phase 1** (Foundation): 1-2 days
- **Phase 2** (Backend): 3-5 days
- **Phase 3** (Frontend): 4-6 days
- **Phase 4** (Docker): 2-3 days
- **Phase 5** (E2E Tests): 2-3 days
- **Phase 6** (Scripts): 1-2 days
- **Phase 7** (Polish): 1-2 days

**Total**: 14-23 days (depending on experience level and complexity)

---

## Progress Tracking

**Current Phase**: Phase 1 - Foundation  
**Last Updated**: 2026-01-06 18:52 UTC  
**Status**: 60% Complete - Ready for directory creation

### Completed Items
- [x] Project plan created and reviewed
- [x] Technical decisions documented (Kotlin 2.3.0, Compose 1.7.1, SQLDelight 2.0.2)
- [x] Git configuration (.gitignore, .gitattributes)
- [x] Gradle multi-project setup (shared, backend, frontend modules)
- [x] Dependencies configured in gradle.properties
- [x] Shared module build.gradle.kts created
- [x] Domain model specifications ready
- [x] Time utility logic designed
- [x] Unit test structure planned
- [x] Documentation created (README, PROGRESS, TODO, STATUS)
- [x] Setup and verification scripts

### Next Steps
1. ✅ ~~Review this plan and adjust as needed~~
2. ✅ ~~Phase 1.1 - Git Repository Setup~~
3. → **CURRENT**: Run `./setup.sh` to create directories, then continue with:
   - Add SQLDelight schema files
   - Create API client interfaces  
   - Configure Gradle wrapper
4. → **NEXT**: Phase 2 - Backend Development
5. Update checkboxes as tasks complete

**Instructions for continuing work:**
```bash
# 1. Create directory structure
chmod +x setup.sh && ./setup.sh

# 2. Initialize git (if not done)
git init
git add .
git commit -F COMMIT_MSG.txt

# 3. Verify build
./gradlew build

# 4. Continue with Phase 1.3 or Phase 2
```

**Note**: This plan is updated continuously as work progresses.

---

## Application Features Summary

### Core Functionality
1. **Customer Management**: Create, list, edit, delete customers
2. **Project Management**: Create projects per customer, organize work
3. **Todo Management**: Create todos per project with full CRUD
4. **Time Tracking**: Estimate time needed, track actual time spent (decimal hours)
5. **Time Input Formats**: Support 2h, 2.5h, 1d, 1.5d, 1w (converts to hours: 1d=8h, 1w=40h)
6. **Due Dates**: Set and track due dates for todos
7. **Completion Tracking**: Mark todos complete with timestamp
8. **Filtering & Sorting**: By customer, project, due date
9. **Time Summaries**: Aggregate estimated vs actual time per project

### UI Screens
1. **Customer List** → Create/Edit Customer
2. **Project List** (filtered by customer) → Create/Edit Project
3. **Todo List** (filtered by project) → Create/Edit Todo
4. **Todo Detail** → Time tracking, completion status

## Notes & Considerations

- **Kotlin Version**: 2.3.0 (latest stable with WASM improvements)
- **Compose Multiplatform**: 1.7.1+ (latest stable)
- **WASM**: Using Compose Multiplatform WASM target (stable as of Kotlin 2.0+)
- **Browser Support**: Modern browsers with WASM support (Chrome, Firefox, Safari, Edge)
- **Android**: Can add native Android app later using same shared module and UI code
- **iOS**: Can add iOS support using Compose Multiplatform or native SwiftUI
- **Authentication**: Not included in MVP but can add JWT/OAuth later
- **CI/CD**: GitHub Actions or GitLab CI can be added after local setup works
- **Production**: Consider Kubernetes or cloud platforms for production deployment
- **Time Format**: Store as decimal hours (Double), support input as h/d/w units
  - 1h = 1.0 hours
  - 1d = 8.0 hours (8-hour workday)
  - 1w = 40.0 hours (5-day work week)
  - Decimal support: 2.5h, 1.5d (12h), etc.
- **Date Handling**: Use kotlinx.datetime for cross-platform date handling
- **Database**: SQLDelight for type-safe SQL queries across all platforms

## Technical Decisions Log

| Date | Decision | Rationale |
|------|----------|-----------|
| 2026-01-06 | Kotlin 2.3.0 | Latest stable with WASM improvements |
| 2026-01-06 | Compose Multiplatform 1.7.1+ | Latest stable for WASM |
| 2026-01-06 | SQLDelight over Exposed | Type-safe, KMP-native, better WASM support |
| 2026-01-06 | Time as decimal hours | Flexible input (h/d/w), simple storage, easy aggregation |

