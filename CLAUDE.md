# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Run Commands

```bash
# Development mode with hot reload
mvn quarkus:dev

# Build
mvn clean verify

# Run after build (requires env vars below)
java -jar target/quarkus-app/quarkus-run.jar

# Run unit tests only
mvn test

# Run integration tests (skipped by default)
mvn verify -DskipITs=false

# Run a single test class
mvn test -Dtest=MyTestClass
```

Required environment variables for running:
```bash
export FITBIT_DATASOURCE_JDBC_URL=jdbc:postgresql://...
export FITBIT_DATASOURCE_USERNAME=...
export FITBIT_DATASOURCE_PASSWORD=...
export FITBIT_CLIENT_ID=...
export FITBIT_CLIENT_SECRET=...
export FITBIT_COOKIE_SECRET=...
```

Tests use H2 in-memory DB (configured in `src/test/resources/application.properties`) — no env vars needed for tests.

## Architecture

This is a **Quarkus 3.x (latest)** Java application targeting Java 25. It uses:
- **JAX-RS** (`quarkus-rest`) for REST endpoints, all under the `/app` path prefix (defined in `RestApplication.java`)
- **Qute** (`quarkus-rest-qute`) for server-side HTML templating — templates live in `src/main/resources/templates/` and are injected via `@Inject Template <name>`
- **CDI** for dependency injection; page controllers are `@RequestScoped`
- **Hibernate ORM** with PostgreSQL in production; H2 in tests
- **MicroProfile REST Client** for the Fitbit API and OAuth token exchange

Make classes package level visible and fields private by default.

For plain DTOs, use Java records and the `@RecordBuilder` annotation, which generates a `*Builder` class.

## Package Structure

```
auth/          OAuth2 flow (login, callback, token exchange) + JWT-signed session cookie
fitbitapi/     MicroProfile REST client interfaces for each Fitbit API endpoint
pages/         One subpackage per page (steps, heartrate, cardioscore, azm, activities, profile)
repository/    JPA entities and repositories for each data type
tcx/           JAXB model for parsing TCX (GPS workout) XML files
widgets/       Reusable Chart.js and Leaflet view-model helpers
```

## Data Flow

Each page follows the same pattern:

1. **Controller** (`@RequestScoped`, `@Path("/pages/<name>")`) extends `DateRangeController` (or `BaseController`)
2. Controller reads `sessionCookie`, calls `SessionManager.parseAndVerifyCookie()` to get `SessionData` (userId + access token)
3. Controller checks the repository for cached data; if missing or `refresh=true`, calls the Fitbit API client, stores results, returns them
4. Controller builds a **ViewModel** (a `@RecordBuilder` record) and passes it to the Qute template via `template.data("model", vm)`
5. **Qute template** renders the page; charts use Chart.js via `{model.stepsChart.formatJson(8)}` — the `TemplateExtensions` class provides `.format()`, `.formatDuration()`, `.formatPace()`, and `.formatJson()` methods callable in templates

## Authentication

- OAuth2 with Fitbit (`/app/oauth/login` → `/app/oauth/callback`)
- After token exchange, `SessionData` (userId + access token + expiry) is JSON-serialized, signed as a JWT, and stored as an HTTP-only cookie named `fibtitSession`
- `SessionManager` signs/verifies the cookie using `app.fitbit.cookie.secret`

## Database

Schema is in `src/main/resources/schema.postgresql.sql`. Tables are prefixed `fitbit_*_` with trailing underscores on column names. All time-series tables use `(user_id_, date_)` as composite primary key. The `fitbit_activity_` table has child tables `fitbit_activity_level_` and `fitbit_activity_heartrate_zone_` with FK constraints.

Hibernate is configured with `database.generation=validate` in production (schema must exist) and `update` in tests.
