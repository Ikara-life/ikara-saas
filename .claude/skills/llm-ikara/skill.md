---
description: Display .llm documentation for ikara-saas. Usage: /llm-ikara [topic]
arguments:
  - name: topic
    description: "Topic to show: architecture | conventions | commands | known-issues | openapi | api-conventions | module"
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

## Behaviour

If `$topic` provided:
- Match case-insensitive, accept partial (e.g. `api` → `api-conventions`)
- Read file, display full content
- Note related topics after

If no `$topic`:
- List all topics with one-line descriptions:
  - **architecture** — Services, ports, request paths, caching, security model, external deps
  - **conventions** — DB migrations, jOOQ codegen, DAO/service/controller pattern, DTOs, exceptions
  - **commands** — Build, test, format, migrate, jOOQ generate, Docker/Jib
  - **known-issues** — Flyway path bug, wrong JDBC driver, hardcoded config path, JWT secret in plaintext
  - **openapi** — Swagger/springdoc setup, dependency, endpoints, controller annotations
  - **api-conventions** — HTTP methods, pagination, validation, error handling, endpoint checklist
  - **module** — All modules, deps, Maven coords, profiles, plugins
