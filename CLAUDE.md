# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

@.llm/commands.md
@.llm/architecture.md
@.llm/conventions.md
@.llm/known-issues.md
@.llm/api/openapi.md
@.llm/api/conventions.md
@.llm/modules/ikara-saas.md

## Available Skills

Project-specific slash commands in `.claude/skills/`.

| Command | When to use |
|---|---|
| `/llm-ikara [topic]` | User asks about architecture, conventions, known issues, API design, module deps, or commands. Show topic list if no arg given. |
| `/spotless-config [module-path]` | Add or update the Spotless Maven profile in a module's pom.xml following ikara-saas conventions. |
| `/rebuild-all [module]` | Clean-build all shared libs then services in dependency order. Pass a single module name to rebuild just that one. |

Topics for `/llm-ikara`: `architecture` · `conventions` · `commands` · `known-issues` · `api-conventions` · `openapi` · `module`

## API Design

No visibility tiers. Single flat Swagger group. All endpoints visible in one `/v3/api-docs` spec.

## .llm Folder Convention

Files in `.llm/` are caveman-compressed (token-efficient prose). When editing any `.llm/` file:
- If caveman plugin installed: run `/caveman:compress <filepath>` after edits to recompress.
- If not installed: install via `claude plugin install caveman`, then compress.
- Maintain compressed style manually if plugin unavailable — drop articles/filler, fragments OK.
