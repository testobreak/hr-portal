#!/usr/bin/env bash
# =============================================================================
# HRMS PostgreSQL Restore Utility
# Restores the database in the running Docker container from a SQL or gz file.
# Usage: ./restore.sh <path_to_backup_file>
# =============================================================================
set -euo pipefail

if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <path_to_backup_file.sql.gz | path_to_backup_file.sql>"
    exit 1
fi

BACKUP_FILE="$1"
if [ ! -f "${BACKUP_FILE}" ]; then
    echo "Error: Backup file '${BACKUP_FILE}' not found."
    exit 1
fi

DB_CONTAINER="${POSTGRES_CONTAINER:-hrms-postgres}"
DB_USER="${POSTGRES_USER:-hrms}"
DB_NAME="${POSTGRES_DB:-hrms}"

echo "Restoring ${DB_NAME} in container ${DB_CONTAINER} from ${BACKUP_FILE}..."

if [[ "${BACKUP_FILE}" == *.gz ]]; then
    gunzip -c "${BACKUP_FILE}" | docker exec -i "${DB_CONTAINER}" psql -U "${DB_USER}" -d "${DB_NAME}"
else
    cat "${BACKUP_FILE}" | docker exec -i "${DB_CONTAINER}" psql -U "${DB_USER}" -d "${DB_NAME}"
fi

echo "Restore completed successfully."
