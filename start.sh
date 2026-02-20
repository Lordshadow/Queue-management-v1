#!/usr/bin/env bash
# ─── Queue Management System — Local Dev Startup ─────────────────────────────
# Usage: ./start.sh

set -e

ENV_FILE="$(dirname "$0")/.env"

if [ ! -f "$ENV_FILE" ]; then
    echo "ERROR: .env file not found."
    echo "Copy .env.example to .env and fill in your values."
    exit 1
fi

# Load .env into the current shell (export all vars)
set -a
# shellcheck disable=SC1090
source "$ENV_FILE"
set +a

echo "Environment loaded from .env"
echo "Starting Queue Management System on port ${SERVER_PORT:-8080}..."

exec mvn spring-boot:run
