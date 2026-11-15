# AstroCare Backend

Production-oriented Spring Boot 3 backend for the AstroCare astronaut health monitoring system. The application uses Java 17, MySQL, Spring Data JPA, stateless JWT/BCrypt RBAC, STOMP over WebSockets, and OpenPDF clinical reports.

## Run

Set `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, and a strong `JWT_SECRET` (at least 32 characters). The defaults target `astrocare_db` on localhost and the application listens on port `8081`. Run:

```bash
mvn clean package
java -jar target/astrocare-0.1.0.jar
```

`schema.sql` remains the source of truth (`spring.jpa.hibernate.ddl-auto=none`). Seeding is enabled by default outside the `prod` profile and can be disabled with `ASTROCARE_SEED_ENABLED=false`.

## Authentication and roles

`POST /api/auth/login` accepts `{ "username": "...", "password": "..." }` and returns a bearer JWT. `ROLE_FLIGHT_SURGEON` can list crew, access all telemetry, run simulations, manage anatomy markers, and generate reports. `ROLE_ASTRONAUT` can access only their own telemetry and dispatch SOS events.

## REST and WebSocket endpoints

- `GET /api/astronauts`
- `GET /api/health/latest/{astronautId}`
- `GET /api/health/history/{astronautId}` with optional ISO-8601 `from` and `to`
- `POST /api/demo/simulate` with `NORMAL`, `WARNING`, `CRITICAL`, or `EVA_STRAIN`
- `GET|POST /api/anatomy/markers`
- `POST /api/emergency/sos`
- `GET /api/reports/pdf/{astronautId}`
- STOMP endpoint `/ws`; subscribe to `/topic/telemetry/{astronautId}` for simulated telemetry broadcasts

## Package layout

The code is strictly separated into `config`, `security`, `controller`, `service`, `repository`, `entity`, `dto`, and `exception` packages. Controllers are routing-only; persistence, authorization checks, simulation, PDF generation, and WebSocket publication reside in services.

Seeded demo users retain the original usernames and share the configured seed password: `surgeon.vance`, `surgeon.rivera`, `astro.okonkwo`, `astro.menon`, `astro.voss`, and `astro.arai`.
