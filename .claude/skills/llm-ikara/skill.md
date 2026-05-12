---
description: Display .llm documentation for ikara-saas. Usage: /llm-ikara [topic]
arguments:
  - name: topic
    description: "Topic to show: architecture | conventions | commands | known-issues | openapi | api-conventions | module | sql | jooq"
    required: false
---

Read and display the requested `.llm/` documentation file for ikara-saas.

## Topic → File Map

| Topic | File |
|---|---|
| `architecture` | `.llm/architecture.md` |
| `conventions` | `.llm/conventions.md` |
| `commands` | `.llm/commands.md` |
| `known-issues` | `.llm/known-issues.md` |
| `openapi` | `.llm/api/openapi.md` |
| `api-conventions` | `.llm/api/conventions.md` |
| `module` | `.llm/modules/ikara-saas.md` |
| `sql` | `.llm/conventions.md` (Database Migrations section) |
| `jooq` | `.llm/conventions.md` (jOOQ Code Generation section) |

## Behaviour

If `$topic` provided:
- Match case-insensitive, accept partial (e.g. `api` → `api-conventions`, `sql` → conventions DB section, `jooq` → conventions jOOQ section)
- Read file, display full content (or relevant section for `sql`/`jooq`)
- Note related topics after

If no `$topic`:
- List all topics with one-line descriptions:
  - **architecture** — Services, ports, request paths, caching, security model, external deps
  - **conventions** — DB migrations, SQL patterns, jOOQ codegen, DAO/service/controller, DTOs, exceptions, OpenAPI
  - **commands** — Build, test, format, migrate, jOOQ generate, Docker/Jib
  - **known-issues** — Flyway path, hardcoded config path, JWT secret, springdoc base64 fix, unused deps
  - **openapi** — springdoc 3.x setup, dependency, endpoints, fix for byte[] base64 bug, controller annotations
  - **api-conventions** — HTTP methods, pagination, validation, error handling, endpoint checklist
  - **module** — All modules, deps, Maven coords, profiles, plugins
  - **sql** — SQL table conventions, naming, constraints, column patterns (shortcut to conventions)
  - **jooq** — jOOQ codegen command, forcedTypes, enum mapping (shortcut to conventions)
