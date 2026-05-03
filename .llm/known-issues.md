# Known Issues

## 1. Flyway Cannot Find Migrations — Wrong Directory Name

**File**: `security/src/main/resources/db.migration/V1__init.sql`
**Symptom**: Flyway runs on startup, finds no migration files, schema never created.
**Root cause**: Files in `db.migration/` (dot) but `spring.flyway.locations: classpath:db/migration` expects slash. Java classpath treats as different paths.
**Fix**: Rename `db.migration` → `db/migration` (create `db/` subdir, move `migration/` in), or change Flyway location to `classpath:db.migration`.

---

## 2. Wrong JDBC Driver Class in Flyway Config

**File**: `configfiles/core.yml:7`, `configfiles/security.yml:7`
**Symptom**: Flyway fails at startup with `ClassNotFoundException: com.mysql.cj.jdbc.Driver`.
**Root cause**: `spring.flyway.driver-class-name: com.mysql.cj.jdbc.Driver` — MySQL driver on PostgreSQL database.
**Fix**: Change to `spring.flyway.driver-class-name: org.postgresql.Driver` in both `core.yml` and `security.yml`.

---

## 3. Config Server Local Path Hardcoded to Different Machine

**File**: `config/src/main/resources/application-local.yml:2`
**Symptom**: Config server fails in local profile — `file:///Users/cepl/IdeaProjects/ikara-saas/configfiles` doesn't exist on most machines.
**Root cause**: Absolute path hardcoded to original dev's machine.
**Fix**: Use relative path or env var: `file:///${CONFIG_FILES_PATH:./configfiles}`, set `CONFIG_FILES_PATH` per developer.

---

## 4. JWT Secret Committed in Plaintext

**File**: `configfiles/application.yml:7`
**Symptom**: Token signing key `ficity_secret_token_for_the_new_saas_application_it_requires_a_very_long_key_to_sign_tokens` committed to version control.
**Root cause**: Dev secret checked in, no secrets management.
**Fix**: Move to env var `JWT_KEY`, reference as `jwt.key: ${JWT_KEY}` in `application.yml`. Rotate secret before any production deployment.

---

## 5. Gateway Routes Configured But No Gateway Service

**File**: `configfiles/gateway.yml`
**Symptom**: Routes for `core` and `security` exist in config but no `gateway/` module in repo. Config server serves file to nothing.
**Root cause**: Gateway planned, not yet created.
**Fix**: Create `gateway/` module with `spring-cloud-starter-gateway`, or remove `gateway.yml` until service exists.

---

## 6. RabbitMQ Dependency Declared But Unused

**Files**: `core/pom.xml`, `security/pom.xml`
**Symptom**: `spring-rabbit` on classpath; Spring auto-config tries to connect to RabbitMQ on startup. Fails if RabbitMQ not running locally.
**Root cause**: Dependency added speculatively, no producers or consumers implemented.
**Fix**: Remove `spring-rabbit` until needed, or add `spring.rabbitmq.listener.simple.auto-startup: false` and `spring.autoconfigure.exclude: org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration` to block auto-connect.

---

## 7. `core` Module Is an Empty Skeleton

**File**: `core/src/main/java/studio/ikara/core/CoreApplication.java`
**Symptom**: `core` starts, exposes no endpoints, does nothing.
**Root cause**: Module is placeholder, business logic not implemented.
**Fix**: Not bug — but `core/pom.xml` includes unused dependencies (jOOQ, RabbitMQ, springdoc, Flyway) adding startup overhead and failure risk.

---

## 8. Feign Client Config Without Feign Dependency

**File**: `configfiles/application.yml` (feign.client.config.*), no `spring-cloud-starter-openfeign` in any pom.xml
**Symptom**: Feign timeout properties loaded but ignored — no Feign clients exist.
**Root cause**: Config added anticipating Feign, no clients implemented.
**Fix**: Add `spring-cloud-starter-openfeign` + `@EnableFeignClients` when needed, or remove dead config.