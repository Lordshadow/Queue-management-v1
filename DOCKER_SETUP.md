# Docker Setup Guide

This document explains how to use Docker to run the Queue Management System backend.

## Prerequisites

- Docker ([Install here](https://docs.docker.com/get-docker/))
- Docker Compose ([Install here](https://docs.docker.com/compose/install/))

## Quick Start

### 1. Build and Start Services

```bash
docker-compose up -d
```

This command will:
- Build the Spring Boot application image
- Start MySQL database container
- Start the application container
- Initialize the database with schema and data

### 2. Check Service Status

```bash
docker-compose ps
```

You should see:
- `queue-management-db` (healthy)
- `queue-management-app` (healthy after 40s startup period)

### 3. Access the Application

- **API**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health

### 4. View Logs

```bash
# All services
docker-compose logs -f

# Only app
docker-compose logs -f app

# Only database
docker-compose logs -f db
```

## Common Commands

### Stop Services

```bash
docker-compose down
```

### Stop and Remove Volumes (Hard Reset)

```bash
docker-compose down -v
```

### Rebuild Image

```bash
docker-compose build --no-cache
```

### Rebuild and Restart

```bash
docker-compose up -d --build
```

## Database Access

### From Host Machine

```bash
mysql -h 127.0.0.1 -P 3306 -u queue_user -pqueue_pass123 queue_management
```

### From Inside App Container

```bash
docker exec -it queue-management-app bash
# Then inside container:
mysql -h db -u queue_user -pqueue_pass123 queue_management
```

## Environment Variables

The `.env` file is automatically loaded by the application. The docker-compose.yml uses:

- **Database credentials** - Matches the MySQL service configuration
- **JWT settings** - Read from .env file
- **Mail configuration** - Read from .env file (ensure these are set for email functionality)
- **Frontend URL** - For CORS configuration

If you need to override environment variables, either:
1. Update `.env` file and restart
2. Modify the `environment` section in `docker-compose.yml`

## Troubleshooting

### Application won't connect to database

```bash
# Check if database is healthy
docker-compose ps db

# View database logs
docker-compose logs db
```

Wait for the database to be fully initialized (health check passing).

### Permission denied while building

```bash
sudo docker-compose up -d
```

### Port Already in Use

If port 8080 or 3306 is already in use:

Edit `docker-compose.yml`:
```yaml
app:
  ports:
    - "8081:8080"  # Change left side to unused port

db:
  ports:
    - "3307:3306"  # Change left side to unused port
```

### Clear Docker Cache

```bash
docker system prune -a
docker-compose build --no-cache
```

## Production Considerations

For production deployment:

1. Use environment-specific `.env` files (`.env.production`)
2. Set strong passwords for database and JWT secret
3. Use external secrets management (e.g., Docker Secrets, Vault)
4. Configure backups for the `db_data` volume
5. Use restart policies:
   ```yaml
   services:
     app:
       restart: unless-stopped
     db:
       restart: unless-stopped
   ```
6. Set resource limits:
   ```yaml
   services:
     app:
       deploy:
         resources:
           limits:
             cpus: '1'
             memory: 1G
     db:
       deploy:
         resources:
           limits:
             cpus: '1'
             memory: 2G
   ```

## Docker Image Details

- **Build Stage**: `maven:3.9-eclipse-temurin-21` - Compiles the application
- **Runtime Stage**: `eclipse-temurin:21-jre-noble` - Lightweight Java 21 runtime
- **Multi-stage build**: Reduces final image size by ~70%

## Health Checks

Both services have health checks configured:

- **Database**: `mysqladmin ping`
- **Application**: Spring Boot actuator endpoint `/actuator/health`

Services won't reach "healthy" status until health checks pass.
