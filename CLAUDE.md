# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Run Commands

```bash
# Development mode with hot reload
mvn quarkus:dev -Dquarkus.http.port=8080

# Build
mvn clean verify

# Run after build
java -Dquarkus.http.port=8080 -jar target/quarkus-app/quarkus-run.jar

# Run unit tests
mvn test

# Run integration tests (skipped by default)
mvn verify -DskipITs=false
```

## Architecture

This is a **Quarkus 3.x** Java application targeting Java 25. It uses:
- **JAX-RS** (`quarkus-rest`) for REST endpoints, all under the `/app` path prefix (defined in `RestApplication.java`)
- **Qute** (`quarkus-rest-qute`) for server-side HTML templating — templates live in `src/main/resources/templates/` and are injected via `@Inject Template <name>`
- **CDI** for dependency injection; controllers are `@RequestScoped`
- **View model pattern**: controller creates a POJO view model and passes it to the Qute template
- Static files served from `src/main/resources/META-INF/resources/`

The application artifact is published to a custom Maven repository at `https://code.phoswald.ch/api/packages/philip/maven`.
