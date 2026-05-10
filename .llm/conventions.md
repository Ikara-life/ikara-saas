# Conventions

## Database Migrations (Flyway)

- Migration files go in: `<module>/src/main/resources/db.migration/` (dot-separated — matches actual files; see known-issues.md for path bug)
- Naming: `V{n}__{description}.sql` (double underscore)
- Schema prefix all objects: `SECURITY.` or `CORE.` — never unqualified names
- Use provided ID generation function: `SECURITY_NEXT_ID()` / `CORE_NEXT_ID()` — custom snowflake (epoch 2023-01-01, shard 1)
- Add `BEFORE UPDATE` trigger for `UPDATED_AT` on every new table (copy pattern from `V1__init.sql`)
- Current highest migration: security=V1, core=none

## jOOQ Code Generation

- Run `mvn generate-sources -P jooq` after schema changes
- Security pattern: `SECURITY_.*` tables → generates into `studio.ikara.security.jooq.*`
- Core pattern: `CORE_.*` tables → generates into `studio.ikara.core.jooq.*`
- Never hand-edit generated files in `jooq/` packages — overwritten on next generation
- New table = new DAO extending `AbstractUpdatableDAO<Record, IdType, DTO>`

## DAO / Service / Controller Pattern

Three-layer abstract hierarchy — always extend, never bypass:
1. `AbstractJOOQDataService` / `AbstractJOOQUpdatableDataService` — business logic
2. `AbstractJOOQDataController` / `AbstractJOOQUpdatableDataController` — REST endpoints
3. DAO for DB access via jOOQ DSLContext

No JPA, Hibernate, Spring Data — project avoids deliberately.

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
- New service → create `configfiles/<service>.yml` + register routes in `configfiles/gateway.yml`

## pom.xml Conventions

- All dep versions in `<properties>` as `package.i.<name>.version` (internal) or `package.o.<name>.version` (external)
- Parent: `org.springframework.boot:spring-boot-starter-parent:3.5.3`
- Spring Cloud BOM: `2025.0.0`
- Java: 25 (security/core); config service: 21
- New shared lib: `mvn clean install` to local Maven repo before dependent services build

## Code Style

- Spotless enforces formatting — run `mvn spotless:apply` before commit
- Lombok throughout — prefer `@Data`, `@Builder`, `@RequiredArgsConstructor`
- Virtual threads enabled — no `ThreadLocal`; use `InheritableThreadLocal` (set by `AbstractBaseConfiguration`)

## CORS

Allowed origins hardcoded in `AbstractBaseConfiguration`: `localhost:3000`, `localhost:8080`. Add origins: extend config class, override CORS bean.

## API Documentation (OpenAPI)

- `springdoc-openapi-starter-webmvc-ui:2.8.17` (Spring Boot 3.x / springdoc v2.x)
- Create `OpenApiConfig` per service — single `OpenAPI` bean, no visibility tiers
- Tag: `@Tag(name = OpenApiConfig.TAG_*)` on controller class; never in `@Operation`