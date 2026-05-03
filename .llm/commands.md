# Commands

## Startup Order

Start in order (each depends on previous):
1. PostgreSQL + Redis + RabbitMQ (external)
2. `eureka/` — port 9999
3. `config/` — port 8888
4. `security/` — port 8001
5. `core/` — port 8005

## Run Locally

```bash
# Each service — run from module directory
cd eureka && mvn spring-boot:run
cd config && mvn spring-boot:run -Dspring-boot.run.profiles=local
cd security && mvn spring-boot:run
cd core && mvn spring-boot:run
```

Config server local profile reads from `configfiles/` at `file:///Users/cepl/IdeaProjects/ikara-saas/configfiles` — update path in `config/src/main/resources/application-local.yml` to match local checkout.

## Build

```bash
# Build shared libs first (services depend on them)
cd commons && mvn clean install
cd commons-jooq && mvn clean install
cd commons-security && mvn clean install

# Build services
cd security && mvn clean package
cd core && mvn clean package
```

## Test

```bash
mvn test                          # all tests in module
mvn test -Dtest=ClassName         # single test class
mvn test -Dtest=ClassName#method  # single method
```

## Lint / Format

```bash
mvn spotless:apply    # apply formatting (Spotless profile)
mvn spotless:check    # check without applying
```
Run from individual module — profile id is `spotless` in each pom.xml.

## jOOQ Code Generation

```bash
# security module — generates from SECURITY.* tables
cd security && mvn generate-sources -P jooq

# core module — generates from CORE.* tables
cd core && mvn generate-sources -P jooq
```

Requires running PostgreSQL with schema pre-created. Reads DB url/credentials from pom.xml jooq profile (localhost:5432, DB name varies per module — see pom.xml `<jdbcUrl>`).

## Database Migrations

Flyway runs automatically on startup when `spring.flyway.enabled: true`.

Manual run:
```bash
mvn flyway:migrate -P jooq
```

Migration files: `security/src/main/resources/db.migration/` (note: dot not slash — see known-issues.md).

## Docker / Container Build

```bash
# Build and push image via Jib (no Docker daemon needed)
mvn jib:build      # push to ghcr.io/ikara-life/<service>
mvn jib:dockerBuild  # build to local Docker daemon
```

Images:
- `ghcr.io/ikara-life/config`
- `ghcr.io/ikara-life/eureka`
- `ghcr.io/ikara-life/security`
- `ghcr.io/ikara-life/core`

## Required Environment Variables

| Variable | Default | Used By |
|---|---|---|
| `CLOUD_CONFIG_SERVER` | `localhost` | core, security, eureka |
| `CLOUD_CONFIG_SERVER_PORT` | `8888` | core, security, eureka |
| `EUREKA_SERVER_HOST` | `localhost` | config, all services |
| `EUREKA_SERVER_PORT` | `9999` | config, all services |
| `SPRING_PROFILE` | `default` | all services |
| `INSTANCE_ID` | `default` | all services |
| `INSTANCE_ENVIRONMENT` | `LOCAL` | all services |

DB credentials + Redis URL served by Config Server from `configfiles/application-default.yml` — no env vars needed for local dev with default profile.