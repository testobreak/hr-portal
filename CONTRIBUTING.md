# Contributing to HR Portal

Thank you for contributing to the HR Portal project.

## Development Workflow

### Prerequisites
- Docker & Docker Compose
- JDK 21+
- Node.js 20+ and npm

### Local Environment Setup
1. Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```
2. Start the infrastructure stack:
   ```bash
   docker compose -f infra/docker-compose.yml --env-file .env up -d
   ```
3. Run the backend:
   ```bash
   # Unix/macOS:
   ./run-backend.sh
   # Windows PowerShell:
   ./run-backend.ps1
   ```
4. Run the frontend:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

## Database Migrations
- Schema changes are managed exclusively via **Flyway** in `backend/src/main/resources/db/migration/`.
- Never modify an existing migration file that has been committed. Add a new `V<number>__<description>.sql` migration.
- Use `timestamptz` for all date-time columns and `numeric(18,4)` with `currency_code` for financial values.
- Primary keys default to `uuidv7()`.

## Commit & Pull Request Guidelines
- Ensure all tests pass: `mvn verify` in `backend/` and `npm run build` in `frontend/`.
- Never commit `.env` or IDE configuration files.
