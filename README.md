# AstroCare data layer (MySQL + Spring Data JPA)

Java 17, Spring Boot 3.3.x, MySQL 8.0.16+.

## Run
1. Start MySQL. The datasource URL has `createDatabaseIfNotExist=true`.
2. Set env vars (or edit `application.properties`): `DB_HOST DB_PORT DB_NAME DB_USER DB_PASSWORD`
3. `mvn spring-boot:run`

`schema.sql` runs on startup (`CREATE TABLE IF NOT EXISTS`), Hibernate DDL is off (`ddl-auto=none`).
The seeder runs once, skipped if `surgeon.vance` exists, and never under profile `prod`.
There is no web layer yet, so the app exits after seeding.

## Seeded logins
All seeded accounts share `astrocare.seed.default-password` (env `ASTROCARE_SEED_PASSWORD`),
stored as BCrypt (cost 12). Change it, and never seed a production database.

| Username | Role |
|---|---|
| surgeon.vance, surgeon.rivera | ROLE_FLIGHT_SURGEON |
| astro.okonkwo (ATLAS), astro.menon (HELIX), astro.voss (ZENITH), astro.arai (ORBIT) | ROLE_ASTRONAUT |

## Telemetry caveat
Seeded vitals are SIMULATED (Gaussian noise around per-astronaut resting baselines, synthetic PQRST ECG).
Ranges were chosen to look like resting in-flight values from Inspiration4-era literature, but they were NOT
pulled from the OSDR dataset. Validate against OSDR before using for anything beyond demos.

## Notes
- Table names are lowercase snake_case (MySQL on Linux is case-sensitive).
- `vital_signs.ecg_lead_ii` is a JSON array of mV samples. Use `VitalSignRepository.findSummariesSince`
  for charts to avoid loading ECG arrays.
- Anatomical marker coordinates are normalised 0..1 on your diagram.
