# Docker Compose Setup

This directory contains the Docker Compose configuration for running the full KMP Playground application stack.

## Services

The application consists of three services:

1. **PostgreSQL Database** (`postgres`)
   - Image: `postgres:15-alpine`
   - Port: 5432 (configurable)
   - Persistent volume for data

2. **Backend API** (`backend`)
   - Ktor server with REST API
   - Port: 8080 (configurable)
   - Connects to PostgreSQL database

3. **Frontend UI** (`frontend`)
   - Compose Multiplatform WASM application
   - Served by nginx
   - Port: 3000 (configurable)
   - Proxies API requests to backend

## Quick Start

### 1. Setup Environment Variables

```bash
cd docker
cp .env.example .env
# Edit .env if you need to change default values
```

### 2. Start All Services

```bash
# From the docker directory
docker-compose up

# Or run in detached mode
docker-compose up -d
```

### 3. Access the Application

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **Database**: localhost:5432

### 4. Stop Services

```bash
# Stop services (keeps data)
docker-compose stop

# Stop and remove containers (keeps data in volumes)
docker-compose down

# Stop and remove everything including volumes
docker-compose down -v
```

## Configuration

### Environment Variables

Edit `.env` file to configure:

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_NAME` | PostgreSQL database name | `todos` |
| `DB_USER` | PostgreSQL username | `postgres` |
| `DB_PASSWORD` | PostgreSQL password | `postgres` |
| `DB_PORT` | PostgreSQL port | `5432` |
| `DB_MAX_POOL_SIZE` | Connection pool size | `10` |
| `BACKEND_PORT` | Backend API port | `8080` |
| `FRONTEND_PORT` | Frontend UI port | `3000` |

### Build from Scratch

```bash
# Rebuild all images
docker-compose build

# Rebuild and start
docker-compose up --build

# Rebuild specific service
docker-compose build backend
docker-compose build frontend
```

## Troubleshooting

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f postgres
```

### Check Service Health

```bash
# Check status
docker-compose ps

# Check backend health
curl http://localhost:8080/health

# Check frontend health
curl http://localhost:3000/health
```

### Database Access

```bash
# Connect to PostgreSQL
docker-compose exec postgres psql -U postgres -d todos

# View tables
docker-compose exec postgres psql -U postgres -d todos -c "\dt"
```

### Reset Database

```bash
# Stop services
docker-compose down

# Remove database volume
docker volume rm docker_postgres_data

# Start fresh
docker-compose up
```

## Development Workflow

### 1. Development Mode

For development, you might want to run services individually:

```bash
# Start only database
docker-compose up postgres

# Run backend locally with Gradle
./gradlew :backend:run

# Run frontend locally with dev server
./gradlew :frontend:wasmJsRun
```

### 2. Hot Reload

Frontend and backend changes require rebuilding the Docker images. For faster development:

- Use Gradle tasks for backend (`./gradlew :backend:run`)
- Use WASM dev server for frontend (`./gradlew :frontend:wasmJsRun`)
- Keep database in Docker for consistency

### 3. Testing Changes

```bash
# Rebuild specific service
docker-compose up --build backend

# Or rebuild and restart all
docker-compose up --build
```

## Network Architecture

```
┌─────────────────────────────────────────┐
│         Frontend (nginx:80)             │
│    Compose WASM UI + Static Assets      │
│         Port: 3000 → 80                 │
└─────────────┬───────────────────────────┘
              │ HTTP Proxy /api/*
              ↓
┌─────────────────────────────────────────┐
│       Backend (Ktor:8080)               │
│     REST API + Business Logic           │
│         Port: 8080 → 8080               │
└─────────────┬───────────────────────────┘
              │ JDBC Connection
              ↓
┌─────────────────────────────────────────┐
│    PostgreSQL Database (5432)           │
│        Data Persistence                 │
│         Port: 5432 → 5432               │
└─────────────────────────────────────────┘
```

All services communicate via the `kmp-network` Docker network.

## Production Considerations

For production deployment:

1. **Change default passwords** in `.env`
2. **Use secrets management** instead of `.env` file
3. **Configure SSL/TLS** for frontend (add nginx SSL configuration)
4. **Set up backup** for PostgreSQL volume
5. **Configure resource limits** in docker-compose.yml
6. **Use production-grade logging** and monitoring
7. **Configure CORS** appropriately in backend
8. **Set up reverse proxy** (e.g., Traefik, nginx) for SSL termination

## Volumes

- `postgres_data`: Persistent storage for PostgreSQL database
  - Location: Docker volume (managed by Docker)
  - Persists across container restarts
  - Removed only with `docker-compose down -v`

## Health Checks

All services have health checks configured:

- **PostgreSQL**: `pg_isready` command
- **Backend**: HTTP GET `/health`
- **Frontend**: HTTP GET `/health`

Health checks ensure services are ready before dependent services start.
