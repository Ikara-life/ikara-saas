# Module Reference — ikara-saas

Multi-module repo. All modules under `studio.ikara` groupId. Spring Boot 4.0.6 parent.

## Parent POM (inherited by all modules)

```
org.springframework.boot:spring-boot-starter-parent:4.0.6
spring-cloud.version: 2025.1.1
java.version: 25 (security/core); config: 21
```

---

## Shared Libraries (install before building services)

### `commons` — `studio.ikara:commons:0.0.1-SNAPSHOT`

Base Spring config, Redis+Caffeine cache, BCrypt, CORS, exception handling, JSON config.

Key deps:
| Dep | Purpose |
|---|---|
| `spring-boot-starter-web` | Web MVC |
| `spring-boot-starter-security` | Spring Security |
| `spring-boot-starter-cache` | Cache abstraction |
| `spring-boot-starter-validation` | Jakarta validation |
| `spring-boot-starter-actuator` | Health/metrics |
| `io.lettuce:lettuce-core` | Redis async client |
| `com.github.ben-manes.caffeine:caffeine` | L1 in-memory cache |
| `com.google.code.gson:gson` | JSON serialization |
| `org.springframework:spring-aspects` | AOP |
| `spring-data-commons` | Pageable, Page types |
| `lombok` | Code generation (optional) |

### `commons-jooq` — `studio.ikara:commons-jooq:0.0.1-SNAPSHOT`

Abstract DAO/service/controller layers, DSLContext wiring.

Key deps:
| Dep | Purpose |
|---|---|
| `org.jooq:jooq` | jOOQ (version managed by Spring Boot parent) |
| `org.postgresql:postgresql` | PostgreSQL JDBC driver |
| `spring-boot-starter-jdbc` | JDBC template |
| `spring-cloud-starter-config` | Config client |
| `studio.ikara:commons` | Base config |
| `spring-data-commons` | Pageable |

### `commons-security` — `studio.ikara:commons-security:0.0.1-SNAPSHOT`

JWT util, `JWTTokenFilter`, `ContextUser`, security filter chain interface.

Key deps:
| Dep | Purpose |
|---|---|
| `io.jsonwebtoken:jjwt-api:0.12.6` | JWT API |
| `io.jsonwebtoken:jjwt-impl:0.12.6` | JWT impl (runtime) |
| `io.jsonwebtoken:jjwt-jackson:0.12.6` | JWT JSON (runtime) |
| `spring-boot-starter-security` | Spring Security |
| `studio.ikara:commons` | Base config |
| `spring-data-commons` | Slice/Page |

---

## Services

### `config` — `studio.ikara:config:1.1.0`

Packaging: jar. Port 8001. Config Server.

Key deps: `spring-cloud-config-server`, `spring-cloud-starter-netflix-eureka-client`, `spring-boot-starter-actuator`

Plugins: `jib-maven-plugin:3.3.0` (→ `ghcr.io/ikara-life/config`), `flyway-maven-plugin`, `spring-boot-maven-plugin`

---

### `eureka` — `studio.ikara:eureka:1.1.0`

Packaging: jar. Port 9999. Service discovery.

Key deps: `spring-cloud-starter-netflix-eureka-server`, `spring-cloud-config-client`, `spring-cloud-starter-config`, `spring-boot-starter-actuator`

Plugins: `jib-maven-plugin:3.3.0` (→ `ghcr.io/ikara-life/eureka`), `spring-boot-maven-plugin`

---

### `security` — `studio.ikara:security:1.1.0`

Packaging: jar. Port 8001. Auth + user management.

Key deps:
| Dep | Version | Purpose |
|---|---|---|
| `studio.ikara:commons` | 0.0.1-SNAPSHOT | Base config |
| `studio.ikara:commons-jooq` | 0.0.1-SNAPSHOT | jOOQ DAO layer |
| `studio.ikara:commons-security` | 0.0.1-SNAPSHOT | JWT/security filter |
| `org.jooq:jooq` | 3.20.5 | jOOQ |
| `org.jooq:jooq-codegen` | 3.20.5 | Code gen |
| `com.mysql:mysql-connector-j` | (managed) | MySQL JDBC driver |
| `org.flywaydb:flyway-core` | (managed) | Migrations |
| `org.springdoc:springdoc-openapi-starter-webmvc-ui` | 3.0.3 | Swagger UI |
| `spring-boot-starter-web` | (managed) | Web MVC |
| `spring-boot-starter-security` | (managed) | Spring Security |
| `spring-boot-starter-cache` | (managed) | Cache |
| `spring-boot-starter-jdbc` | (managed) | JDBC |
| `spring-boot-starter-actuator` | (managed) | Health/metrics |
| `spring-boot-starter-validation` | (managed) | Validation |
| `spring-cloud-starter-config` | (managed) | Config client |
| `spring-cloud-starter-netflix-eureka-client` | (managed) | Service discovery |
| `spring-amqp:spring-rabbit` | (managed) | RabbitMQ (unused — see known-issues) |
| `spring-retry` | (managed) | Retry |
| `org.springframework:spring-aspects` | (managed) | AOP |
| `lombok` | (managed) | Code gen |

Profiles:
| Profile | Purpose |
|---|---|
| `jooq` | jOOQ codegen from `SECURITY_.*` tables, schema `security`, DB `ikara` |
| `spotless` | Code formatting check/apply |

Plugins: `jib-maven-plugin:3.4.6` (→ `ghcr.io/ikara-life/security`), `maven-compiler-plugin`, `flyway-maven-plugin`, `spring-boot-maven-plugin`

DB schema: `security`. Highest Flyway version: V1.

Tables:
| Table | Purpose |
|---|---|
| `SECURITY_CLIENT_TYPE` | Lookup: SYS (System), BUS (Business) |
| `SECURITY_CLIENTS` | Tenant organisations; CLIENT_ID=NULL means platform-level |
| `SECURITY_USERS` | Users; CLIENT_ID scopes to tenant |
| `SECURITY_ROLES` | Roles (ADMIN, INSTRUCTOR, USER); CLIENT_ID=NULL = platform-wide |
| `SECURITY_PERMISSIONS` | Permissions (e.g. USER_CREATE); CLIENT_ID=NULL = platform-wide |
| `SECURITY_ROLE_PERMISSIONS` | Many-to-many role ↔ permission |
| `SECURITY_USER_ROLES` | Many-to-many user ↔ role |

Multi-tenancy rule: all data queries must filter by `CLIENT_ID`. Platform accounts have `CLIENT_ID = NULL`.

Authority string format (generated at login — never stored):
- Role: `Authorities.{CLIENT_CODE}.ROLE_{NAME}` e.g. `Authorities.DEMO_STUDIO.ROLE_ADMIN`
- Permission: `Authorities.{CLIENT_CODE}.{PERMISSION_CODE}` e.g. `Authorities.DEMO_STUDIO.USER_CREATE`
- `AuthoritiesNameUtil` in `commons-security` generates both formats.

Key classes implemented:
| Class | Role |
|---|---|
| `AuthenticationService` | Implements `IAuthenticationService` — login + token validation |
| `UserRegistrationService` | Register new user → auto-authenticate → return `AuthenticationResponse` |
| `UserService` | Extends `AbstractJOOQUpdatableDataService` — CRUD + password check |
| `UserDAO` | jOOQ DAO for `SECURITY_USERS`; `loadAuthorities` uses `AuthoritiesNameUtil` |
| `SecurityConfiguration` | Extends `AbstractJooqBaseConfiguration`, implements `ISecurityConfiguration` |
| `OpenApiConfig` | Single `OpenAPI` bean, `TAG_AUTH = "Authentication"` |
| `AuthoritiesNameUtil` | (commons-security) Generates role/permission authority strings from clientCode + name |

Models: `AuthenticationRequest` (userName, password, rememberMe), `AuthenticationResponse` (ContextUser, accessToken, accessTokenExpiryAt), `UserRegistrationRequest` (emailId, password, phone, name fields → `toUser()` sets userName = emailId).

---

### `core` — `studio.ikara:core:1.1.0`

Packaging: jar. Port 8005. Core business logic (skeleton — no domain logic implemented).

Key deps: same as `security` minus `commons-security`. Adds `studio.ikara:commons` only (no `commons-jooq` or `commons-security`).

Profiles: `jooq` (generates from `CORE_.*` tables, schema `core`), `spotless`

Plugins: `jib-maven-plugin:3.4.6` (→ `ghcr.io/ikara-life/core`), `maven-compiler-plugin`, `flyway-maven-plugin`, `spring-boot-maven-plugin`

DB schema: `core`. No tables or migrations yet.

---

## Runtime External Dependencies (all services)

| System | Version | Purpose |
|---|---|---|
| MySQL | 8.x+ | Primary data store |
| Redis | any recent | L2 cache + pub/sub |
| RabbitMQ | any recent | Message broker (declared, unused) |
| Netflix Eureka | via Spring Cloud 2025.1.1 | Service discovery |
| Spring Cloud Config | via Spring Cloud 2025.1.1 | Centralized config |

---

## Notable Constraints

- Java 24 (except config module: 21) — virtual threads enabled
- jOOQ 3.20.5 locked in pom.xml — overrides Spring Boot parent managed version
- No JPA/Hibernate — deliberate; jOOQ only
- `langchain4j.version: 1.1.0` and `playwright.version: 1.53.0` declared in `core` and `security` properties — not deps yet, planned