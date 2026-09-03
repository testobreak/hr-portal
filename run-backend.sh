#!/usr/bin/env bash
# =============================================================================
# HRMS Backend Runner (macOS / Linux)
# Loads environment variables from .env and starts Spring Boot with local profile
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="${SCRIPT_DIR}/.env"

if [ -f "${ENV_FILE}" ]; then
    echo "Loading environment from ${ENV_FILE}..."
    set -a
    # shellcheck disable=SC1090
    source <(grep -E -v '^(#|[[:space:]]*$)' "${ENV_FILE}" | sed -E 's/^[[:space:]]*//')
    set +a
else
    echo "Warning: .env file not found at ${ENV_FILE}. Using default system environment."
fi

cd "${SCRIPT_DIR}/backend"

if [ -x "./mvnw" ]; then
    ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
else
    mvn spring-boot:run -Dspring-boot.run.profiles=local
fi
