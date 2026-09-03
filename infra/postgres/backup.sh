#!/usr/bin/env bash
# =============================================================================
# HRMS PostgreSQL Backup Utility
# Dumps the database from the running Docker container to a timestamped file.
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKUP_DIR="${SCRIPT_DIR}/backups"
TIMESTAMP="$(date +'%Y%m%d_%H%M%S')"
BACKUP_FILE="${BACKUP_DIR}/hrms_backup_${TIMESTAMP}.sql.gz"

mkdir -p "${BACKUP_DIR}"

DB_CONTAINER="${POSTGRES_CONTAINER:-hrms-postgres}"
DB_USER="${POSTGRES_USER:-hrms}"
DB_NAME="${POSTGRES_DB:-hrms}"

echo "Starting backup of ${DB_NAME} from container ${DB_CONTAINER}..."

docker exec -t "${DB_CONTAINER}" pg_dump -U "${DB_USER}" -d "${DB_NAME}" --clean --if-exists | gzip > "${BACKUP_FILE}"

echo "Backup completed successfully: ${BACKUP_FILE}"
