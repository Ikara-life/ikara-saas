# Conventions

## Database Migrations (Flyway)

Migration path: `<module>/src/main/resources/db/migration/` (slash-separated — `db/migration` NOT `db.migration`)
Naming: `V{n}__{description}.sql` (double underscore, e.g. `V1__init.sql`, `V2__roles_permissions.sql`)
Current highest: security=V2, core=none

### SQL Table Conventions

Every migration file follows this pattern exactly:

```sql
-- 1. Schema (in V1 only, once per service)
CREATE SCHEMA IF NOT EXISTS `security` DEFAULT CHARACTER SET utf8mb4;

-- 2. Tables — use IF NOT EXISTS, backtick-quote schema + table + column names
CREATE TABLE IF NOT EXISTS `security`.`security_<name>`
(
    `ID`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    -- ... domain columns ...
    `CREATED_BY`  BIGINT UNSIGNED          DEFAULT NULL     COMMENT 'ID of the user who created this row',
    `CREATED_AT`  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Time when this row is created',
    `UPDATED_BY`  BIGINT UNSIGNED          DEFAULT NULL     COMMENT 'ID of the user who updated this row',
    `UPDATED_AT`  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Time when this row is updated',
    PRIMARY KEY (`ID`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
```

Key rules:
- Engine always `InnoDB`, charset always `utf8mb4`
- `ID`: `BIGINT UNSIGNED NOT NULL AUTO_INCREMENT` — MySQL auto-increments (Snowflake ID set by Java before INSERT, so actual value doesn't rely on auto-increment for normal rows)
- Every column has a `COMMENT`
- Audit columns (`CREATED_BY`, `CREATED_AT`, `UPDATED_BY`, `UPDATED_AT`) on every table, in that order, at the bottom
- `UPDATED_AT` uses `ON UPDATE CURRENT_TIMESTAMP` — no trigger needed (MySQL handles it)
- `ENUM` columns: define allowed values inline e.g. `ENUM('ACTIVE','INACTIVE','DELETED','LOCKED','PASSWORD_EXPIRED')`

### Constraint Naming

| Type | Pattern | Example |
|---|---|---|
| Unique key | `UK_{TABLE}_{COLUMN}` | `UK_SECURITY_USERS_USER_NAME` |
| Unique key (composite) | `UK{n}_{TABLE}_{COL1}_{COL2}` | `UK1_USER_AUTHORITIES_USER_ID_AUTHORITY_ID` |
| Foreign key | `FK{n}_{CHILD_TABLE}_{COLUMN}_{PARENT_TABLE}_{REF_COL}` | `FK1_USER_AUTHORITIES_USER_ID_USERS_ID` |

## jOOQ Code Generation

Run after schema changes:
```bash
mvn generate-sources -P jooq   # from module directory
```

Requires running MySQL with schema already created (Flyway must have run).

Config in `pom.xml` jooq profile:
- `<name>org.jooq.meta.mysql.MySQLDatabase</name>` — MySQL meta provider
- `<includes>SECURITY_.*</includes>` — pattern per module (CORE_.* for core)
- `<inputSchema>${database.schema}</inputSchema>` — schema name
- Generated into `${package.c.jooq}` = `studio.ikara.<service>.jooq.*`
- Target dir: `${dir.main.java}` (same source tree, excluded from Spotless)

### Enum Forced Types

Map DB ENUM column to Java enum via `<forcedType>` in pom.xml jooq profile:

```xml
<forcedTypes>
    <forcedType>
        <name>MY_ENUM_TYPE</name>
        <userType>studio.ikara.security.enums.MyEnum</userType>
        <includeExpression>.*\.MY_COLUMN_NAME</includeExpression>
        <includeTypes>.*</includeTypes>
        <enumConverter>true</enumConverter>
    </forcedType>
</forcedTypes>
```

Example in security: `USER_STATUS_CODE` → `studio.ikara.security.enums.UserStatusCode`

Never hand-edit files in `jooq/` package — overwritten on next codegen run.

## DAO / Service / Controller Pattern

Three-layer abstract hierarchy — always extend, never bypass:
1. `AbstractJOOQDataService` / `AbstractJOOQUpdatableDataService` — business logic
2. `AbstractJOOQDataController` / `AbstractJOOQUpdatableDataController` — REST endpoints
3. DAO extending `AbstractUpdatableDAO<Record, IdType, DTO>` for DB access

No JPA, Hibernate, Spring Data — deliberately avoided.

## DTOs

- Extend `AbstractDTO<IdType, CreatedByType>` or `AbstractUpdatableDTO<...>`
- `@JsonIgnore` on credential/secret fields (e.g. `password`)
- Use `@NotNull`, `@Size`, `@Email` from jakarta.validation
- Enum fields: use `UserStatusCode`-style enums with jOOQ forced type config in pom.xml

## Exception Handling

- Throw `GenericException` (from `studio.ikara.commons.exception`) — caught by `ControllerAdvice`, returns RFC 7807 `ProblemDetail`
- No raw `RuntimeException` from service layer
- `ControllerAdvice` in commons handles all uncaught → 500

## Security / JWT

- Token claims: `userId` (Long), `hostName`, `port`, `oneTime`
- Tokens host-bound — token from IP A invalid at IP B
- `ContextUser` lives in `SecurityContextHolder` — cast `Authentication.getPrincipal()` to it
- Internal routes: paths matching `.*internal.*` permitted without auth (defined in `ISecurityConfiguration`)
- Add permitted paths in class implementing `ISecurityConfiguration` — don't modify interface default

## Caching

- Inject `CacheService`, not `CacheManager`
- Use `evict(cacheName, key)` for single entry; broadcasts to Redis pub/sub for cluster-wide invalidation
- Cache codec: `object` (Java serialization) requires `Serializable` on cached objects
- TTL fixed at 5 min (Caffeine) — no per-entry override

## Configuration (Spring Cloud Config)

- Env-specific values in `configfiles/application-default.yml` or `configfiles/<service>.yml`
- No hardcoded DB URLs, secrets, external hosts in service `application.yml`
- YAML rule: never have two sibling top-level keys with same name (e.g. two `spring:` blocks) — YAML last-write-wins and config server 500s
- New service → create `configfiles/<service>.yml` + register routes in `configfiles/gateway.yml`

## pom.xml Conventions

- All dep versions in `<properties>` as `package.i.<name>.version` (internal) or `package.o.<name>.version` (external)
- Parent: `org.springframework.boot:spring-boot-starter-parent:4.0.6`
- Spring Cloud BOM: `2025.1.1`
- Java: 25 (security/core); config service: 21
- New shared lib: `mvn clean install` to local Maven repo before dependent services build

## Code Style

- Spotless enforces formatting — run `mvn spotless:apply` before commit
- Spotless excludes `jooq/` and `generated/` packages (auto-generated, not formatted)
- Lombok throughout — prefer `@Data`, `@Builder`, `@RequiredArgsConstructor`
- Virtual threads enabled — no `ThreadLocal`; use `InheritableThreadLocal` (set by `AbstractBaseConfiguration`)

## CORS

Allowed origins in `AbstractBaseConfiguration`: `localhost:3000`, `localhost:8080`. Add origins: extend config class, override CORS bean.

## API Documentation (OpenAPI)

- springdoc `3.0.3` for Spring Boot 4.x (`springdoc-openapi-starter-webmvc-ui`)
- OpenAPI spec: `GET /v3/api-docs` — returns JSON directly
- Create `OpenApiConfig` per service — single `OpenAPI` bean, no visibility tiers
- Tag: `@Tag(name = OpenApiConfig.TAG_*)` on controller class; never in `@Operation`
- Known fix: `AbstractBaseConfiguration.extendMessageConverters()` moves `ByteArrayHttpMessageConverter` (with `application/json` support) before Jackson — prevents springdoc's `byte[]` spec response being base64-encoded by Jackson 3.x
