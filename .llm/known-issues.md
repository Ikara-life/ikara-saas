# Known Issues

## 1. Old PG Migration in Wrong Directory (Superseded)

**File**: `security/src/main/resources/db.migration/V1__init.sql`
**Status**: Superseded — MySQL migration now at correct path `security/src/main/resources/db/migration/V1__init.sql`. Old PG file in `db.migration/` can be deleted.

---

## 2. ~~Wrong JDBC Driver Class in Flyway Config~~ — RESOLVED

Database switched from PostgreSQL to MySQL. All driver references now correctly set to `com.mysql.cj.jdbc.Driver`.

---

## 3. Config Server Local Path Hardcoded Per Machine

**File**: `config/src/main/resources/application-local.yml:2`
**Symptom**: Config server fails on any machine other than the current dev's — path is `file:///Users/lawbringr/IdeaProjects/ikara/ikara-saas/configfiles`.
**Root cause**: Absolute path hardcoded per developer. Updated from original `cepl` path but still machine-specific.
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

## 8. `ConfigApplication.main()` Missing `public`

**File**: `config/src/main/java/studio/ikara/config/ConfigApplication.java:13`
**Symptom**: `static void main` is package-private — Spring Boot maven plugin may fail to detect entry point; reflection-based launchers (Jib, native) will error.
**Root cause**: `public` modifier omitted.
**Fix**: Change to `public static void main(String[] args)`.

---

## 9. Feign Client Config Without Feign Dependency

**File**: `configfiles/application.yml` (feign.client.config.*), no `spring-cloud-starter-openfeign` in any pom.xml
**Symptom**: Feign timeout properties loaded but ignored — no Feign clients exist.
**Root cause**: Config added anticipating Feign, no clients implemented.
**Fix**: Add `spring-cloud-starter-openfeign` + `@EnableFeignClients` when needed, or remove dead config.