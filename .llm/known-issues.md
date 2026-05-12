# Known Issues

## 1. ~~Old PG Migration File~~ — RESOLVED

Deleted. `security/src/main/resources/db.migration/` directory is gone.

---

## 2. ~~Wrong JDBC Driver Class in Flyway Config~~ — RESOLVED

Database switched from PostgreSQL to MySQL. All driver refs now `com.mysql.cj.jdbc.Driver`.

---

## 3. Config Server Local Path Hardcoded Per Machine

**File**: `config/src/main/resources/application-local.yml:2`
**Symptom**: Config server fails on other machines — path is `file:///Users/lawbringr/IdeaProjects/ikara/ikara-saas/configfiles`.
**Root cause**: Absolute path per developer.
**Fix**: `file:///${CONFIG_FILES_PATH:./configfiles}`, set `CONFIG_FILES_PATH` per dev.

---

## 4. JWT Secret Committed in Plaintext

**File**: `configfiles/application.yml:7`
**Symptom**: Token signing key in version control.
**Root cause**: Dev secret checked in, no secrets management.
**Fix**: Env var `JWT_KEY`, reference as `jwt.key: ${JWT_KEY}`. Rotate before prod.

---

## 5. Gateway Routes Configured But No Gateway Service

**File**: `configfiles/gateway.yml`
**Symptom**: Routes for `core` and `security` exist, no `gateway/` module.
**Fix**: Create `gateway/` module or remove `gateway.yml` until needed.

---

## 6. RabbitMQ Dependency Declared But Unused

**Files**: `core/pom.xml`, `security/pom.xml`
**Symptom**: Spring auto-config tries to connect RabbitMQ on startup. Fails if not running.
**Fix**: Remove `spring-rabbit` until needed, or exclude `RabbitAutoConfiguration`.

---

## 7. `core` Module Is an Empty Skeleton

**File**: `core/src/main/java/studio/ikara/core/CoreApplication.java`
**Status**: Placeholder — no endpoints, unused deps (jOOQ, RabbitMQ, springdoc, Flyway).

---

## 8. `ConfigApplication.main()` Missing `public`

**File**: `config/src/main/java/studio/ikara/config/ConfigApplication.java:13`
**Symptom**: Package-private `main` — Jib/native launchers may fail.
**Fix**: `public static void main(String[] args)`.

---

## 9. Feign Client Config Without Feign Dependency

**File**: `configfiles/application.yml` (feign.client.config.*)
**Symptom**: Feign timeout properties loaded but ignored.
**Fix**: Add `spring-cloud-starter-openfeign` + `@EnableFeignClients` when needed.

---

## 10. springdoc `/v3/api-docs` Returns Base64 Instead of JSON

**Files**: `commons/src/main/java/.../configuration/AbstractBaseConfiguration.java`
**Symptom**: springdoc 3.x returns spec as `byte[]` with `application/json`. Custom `@Bean JacksonJsonHttpMessageConverter` (Jackson 3.x) intercepts and base64-encodes the bytes. Swagger UI shows "Unable to render — no valid version field".
**Root cause**: Jackson 3.x serializes `byte[]` as base64. Custom converter registered as `@Bean` takes priority over `ByteArrayHttpMessageConverter` for `application/json`.
**Fix**: `AbstractBaseConfiguration.extendMessageConverters()` — moves `ByteArrayHttpMessageConverter` (with `application/json` added to supported types) to position 0 in converter chain, before Jackson. `byte[]` responses written raw; Jackson still handles objects.
**Status**: Fixed.
