# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

### Backend (Java/Spring Boot)
```bash
# Run all tests (uses H2 dev profile)
./mvnw clean test -Dspring.profiles.active=dev

# Build without tests
./mvnw clean compile

# Package into JAR
./mvnw package -DskipTests

# Run a single test class
./mvnw test -Dtest=FintrackApplicationTests -Dspring.profiles.active=dev

# Run backend locally with dev profile (H2, no external deps)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Frontend (SvelteKit)
```bash
cd Frontend

# Dev server with hot reload (port 5173)
npm run dev

# Build static assets
npm run build

# Run vitest unit tests
npm run test:unit -- --run

# Lint & format
npm run lint
npm run format
```

### Docker Compose (full stack)
```bash
docker compose up -d --build
# App available at http://localhost:8888
```

## Architecture Overview

**FinTrack** — personal finance tracking app. Spring Boot 3 backend (Java 17) + SvelteKit 5 SPA frontend, served through NGINX.

### Backend package structure (`com.zz.fintrack`)
- **`auth/`** — `POST /api/auth/signup` and `/login`. Issues a JWT as an HttpOnly cookie (`jwt_token`, 7-day expiry) on success.
- **`security/`** — Stateless security config. CORS allows localhost:5173/3000/8888/8080. Permits all to `/api/auth/**`, `/api/health`, Swagger, H2 console. JWT filter extracts the token from the `jwt_token` cookie. BCrypt password encoding.
- **`user/`** — Entity (email, name, password_hash). `GET|PUT|DELETE /api/users/me` for the authenticated user's own profile.
- **`account/`** — Entity with `CASH | BANK | CARD` types, per-user ownership. Full CRUD at `/api/accounts`. Redis-cached by user ID.
- **`category/`** — Entity with `INCOME | EXPENSE` types, per-user ownership. CRUD at `/api/categories`. Optional `?type=` filter. Redis-cached.
- **`tx/`** — Transaction entity with `INCOME | EXPENSE | TRANSFER`, multi-currency support (FX rate snapshot, baseAmount conversion). `POST /api/transactions`, `GET /api/transactions?start=&end=` (paginated), `GET /api/transactions/reports/monthly`, `GET /api/transactions/reports/weekly-totals`, and a dev-only `POST /api/transactions/seed/weekly` endpoint. Java record DTOs in `tx/dto/TransactionDtos`. Optimistic locking via `@Version`.
- **`kafka/`** — AuditProducer publishes to `transaction-events` topic on each transaction create; AuditConsumer logs received events. Topic auto-created (1 partition, 1 replica).
- **`common/`** — Global `@RestControllerAdvice` handling validation errors, EntityNotFoundException, BadCredentialsException, AccessDeniedException, and DataIntegrityViolationException.

### Key patterns
- **Ownership enforcement** — Every controller resolves the user via `@AuthenticationPrincipal`. Service methods like `getOwned()` verify the resource user matches the principal before returning or mutating.
- **JWT in HttpOnly cookie** (not localStorage) — prevents XSS token theft. Extracted and validated in JwtAuthFilter (a OncePerRequestFilter). The frontend sends `credentials: 'include'` to include it automatically.
- **Dual datasource profiles** — Default profile uses PostgreSQL with `ddl-auto: update`. The `dev` profile (activated by `-Dspring-boot.run.profiles=dev`) uses H2 in PostgreSQL-compat mode with `create-drop`.
- **Redis caching** — Account list and category list queries annotated with `@Cacheable`/`@CacheEvict`. Keyed by user ID.
- **Kafka audit logging** — Every transaction create publishes an event string to the `transaction-events` Kafka topic.

### Frontend structure (`Frontend/src/`)
- **`lib/api.js`** — Single `fetchApi()` helper with `credentials: 'include'` and 401 redirect handling. Exports `authApi`, `userApi`, `accountsApi`, `categoriesApi`, `transactionsApi`.
- **Routes** — Login/signup page at `/` (toggles between login/register modes, checks auth on mount). Dashboard at `/dashboard` (accounts sidebar + transactions table, modals for creating entities).
- **Components** — `AccountModal.svelte` and `TransactionModal.svelte` for inline creation.
- Built with Svelte 5 runes mode, `adapter-static` (outputs to `../src/main/resources/static`), vitest + Playwright for testing.

### Infrastructure
- **docker-compose.yml** — PostgreSQL 16 (port 5433), Redis (6380), Zookeeper + Kafka (9092), app container, NGINX (host port 8888 -> app:8080).
- **Dockerfile** — 3-stage: Node 22 builds SvelteKit -> Maven 21 builds Spring Boot JAR -> eclipse-temurin:21-jre-alpine runtime.
- **CI** (`.github/workflows/ci.yml`) — JDK 17 Temurin, `mvnw clean test -Dspring.profiles.active=dev`, then build and package. Triggers on push/PR to `main` and `develop`.
- **Swagger** — Available at `/swagger-ui.html` via springdoc-openapi.
