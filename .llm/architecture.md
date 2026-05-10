# Architecture

## What This Is

Multi-service Spring Boot 3.5.3 SaaS platform (ikara). Services register with Netflix Eureka, get config from Spring Cloud Config Server, use PostgreSQL via jOOQ (no JPA/Hibernate).

## Services

| Service | Port | Role |
|---|---|---|
| `eureka` | 9999 | Service discovery (Netflix Eureka Server) |
| `config` | 8888 | Centralized config server (Spring Cloud Config, serves from `configfiles/`) |
| `security` | 8001 | Authentication, user management, JWT issuance |
| `core` | 8005 | Core business logic (skeleton — no endpoints yet) |

## Shared Libraries

| Lib | Role |
|---|---|
| `commons` | Base Spring config, Redis+Caffeine cache, exception handling, CORS, BCrypt |
| `commons-jooq` | Abstract jOOQ DAO/service/controller layers, DSLContext config |
| `commons-security` | JWT util, security filter chain, `JWTTokenFilter`, `ContextUser` |

## Request Path — Authentication

```
Client → JWTTokenFilter (OncePerRequestFilter)
       → IAuthenticationService.getAuthentication(token, request)
       → JWTUtil.validate() — checks signature, expiry, host binding
       → SecurityContextHolder
```

New request (login/register):
```
Client → (no @RestController yet — services implemented, no HTTP endpoints)
       → AuthenticationService.authenticate() — BCrypt check, token generation
       → UserRegistrationService.registerUser() — validate, create user, auto-authenticate
       → JWTUtil.generateToken() — embeds userId, hostName, port
       → returns AuthenticationResponse{ContextUser, accessToken, accessTokenExpiryAt}
```

## Request Path — Config Bootstrap

```
Service startup
  → spring.config.import: configserver:http://localhost:8888/
  → Config Server fetches from configfiles/ (native profile) or Git (other profiles)
  → Service receives merged properties: application.yml + <service-name>.yml
```

Config files served (all in `configfiles/`):
- `application.yml` — common to all (logging, Eureka, JWT key, feign timeouts, file paths)
- `application-default.yml` — local dev (DB URLs, Redis URL, logging levels, MQ host)
- `core.yml` — datasource + Flyway for core schema
- `security.yml` — datasource + Flyway for security schema
- `gateway.yml` — gateway route table (no gateway service yet)

## Data Layer

No JPA. All DB access via jOOQ `DSLContext`.

- `AbstractDAO` → `AbstractUpdatableDAO` (commons-jooq)
- Concrete: `UserDAO` (security module) — queries `SECURITY.SECURITY_USERS` joined to `SECURITY.SECURITY_AUTHORITIES`
- Code generated from DB: jooq profile in pom.xml generates records into `<module>/src/main/java/.../jooq/`

## Database Schemas

| Schema | Owner | Tables |
|---|---|---|
| `security` | security service | `SECURITY_USERS`, `SECURITY_AUTHORITIES`, `SECURITY_USER_AUTHORITIES` |
| `core` | core service | (no tables yet — skeleton) |

Both schemas on same PostgreSQL instance in dev (localhost:5432, DB `ikara`).

## Caching

Dual-layer in `CacheService`:
- L1: Caffeine (5 min expireAfterAccess, JVM-local)
- L2: Redis (Lettuce async, hash `{prefix}-{cacheName}` → key → `CacheObject`)
- Eviction via Redis pub/sub on `evictionChannel`; message format `cacheName:key` or `cacheName:*`

## Message Broker

RabbitMQ declared (`spring-rabbit` dependency, MQ properties in `application-default.yml`) but **no producers or consumers implemented** yet.

## Security Model

- JWT HS512, issuer `"aplygen"`, custom snowflake-style user IDs
- Tokens bound to issuing host IP — request from different IP fails
- Default expiry 60 min (`jwt.token.default.expiry`), remember-me 1440 min (`jwt.token.rememberme.expiry`)
- JWT secret in `configfiles/application.yml` (plaintext — dev only, must rotate in prod)
- Spring Security: CSRF off, CORS open (`*`), internal routes + Swagger + actuator permitted without auth

## API Documentation

Both `security` and `core` include `springdoc-openapi-starter-webmvc-ui:2.8.17`.
- Spring Boot 3.x → springdoc v2.x artifact (already correct)
- Swagger UI: `http://localhost:<port>/swagger-ui.html`
- `OpenApiConfig` exists in security module — not yet created in core

## External Dependencies

| System | Purpose | Config key |
|---|---|---|
| PostgreSQL | Primary data store | `*.db.url`, `spring.datasource.*` |
| Redis | L2 cache + pub/sub eviction | `redis.url` |
| RabbitMQ | Message broker (not yet used) | `mq.host`, `mq.port` |
| Netflix Eureka | Service discovery | `eureka.client.serviceUrl.defaultZone` |
| Spring Cloud Config | Centralized config | `spring.config.import` |

## Cloud Profiles / Environments

| Profile | Behaviour |
|---|---|
| `default` | Local dev — uses `application-default.yml` from configfiles/ |
| `local` | Config server reads from local filesystem path |

No prod-specific profile files in `configfiles/` yet.

## Virtual Threads

`spring.threads.virtual.enabled: true` in security and core configs. `VirtualThreadExecutor` used in commons-security for async token validation (`IAuthenticationService.getAuthentication` returns `CompletableFuture`).