---
description: Clean-build all ikara-saas shared libs then services in dependency order. Usage: /rebuild-all [module]
arguments:
  - name: module
    description: "Optional single module to rebuild: commons | commons-jooq | commons-security | security | core | config | eureka. Omit to rebuild everything."
    required: false
---

Run `mvn clean install` (or `mvn clean package` for deployable services) on ikara-saas modules in the correct dependency order.

## Build Order

Shared libs must be installed to local Maven repo before services can resolve them:

```
1. commons              → mvn clean install
2. commons-jooq         → mvn clean install
3. commons-security     → mvn clean install
4. config               → mvn clean package
5. eureka               → mvn clean package
6. security             → mvn clean package
7. core                 → mvn clean package
```

## Behaviour

**No argument — full rebuild:**

Run each step sequentially from the repo root. Stop immediately on first failure and report which module failed and the relevant error lines.

```bash
BASE=/Users/lawbringr/IdeaProjects/ikara/ikara-saas

cd "$BASE/commons"          && mvn clean install -q
cd "$BASE/commons-jooq"     && mvn clean install -q
cd "$BASE/commons-security" && mvn clean install -q
cd "$BASE/config"           && mvn clean package -q
cd "$BASE/eureka"           && mvn clean package -q
cd "$BASE/security"         && mvn clean package -q
cd "$BASE/core"             && mvn clean package -q
```

**Single module argument:**

Build only that module. If it is a service (not a lib), remind the user that shared libs must already be installed.

Lib modules: `commons`, `commons-jooq`, `commons-security` → use `mvn clean install`
Service modules: `config`, `eureka`, `security`, `core` → use `mvn clean package`

## Output Format

For each module, print one line before starting it:

```
▶ Building commons...
✓ commons — 4.2s
▶ Building commons-jooq...
✓ commons-jooq — 6.1s
...
✓ All modules built successfully.
```

On failure:
```
▶ Building security...
✗ security — FAILED
[paste the first ERROR lines from Maven output]
```

## Notes

- Use `-q` (quiet) flag to suppress verbose Maven download/compile noise — only errors surface.
- Do NOT use `-DskipTests` unless the user explicitly asks to skip tests.
- Run from the module directory, not the repo root, since there is no parent aggregator pom.
- The repo root is: `/Users/lawbringr/IdeaProjects/ikara/ikara-saas`
