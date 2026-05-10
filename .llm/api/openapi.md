# OpenAPI / Swagger Setup

`springdoc-openapi-starter-webmvc-ui` 2.8.17 (latest as of 2026-04). Spring Boot 3.x — NOT old `springdoc-openapi-ui` 1.x (Spring Boot 2.7).

## Endpoints

| Service | Swagger UI | API JSON |
|---|---|---|
| security (8001) | `http://localhost:8001/swagger-ui.html` | `http://localhost:8001/v3/api-docs` |
| core (8005) | `http://localhost:8005/swagger-ui.html` | `http://localhost:8005/v3/api-docs` |

Postman import: **Import → Link → paste URL**.

---

## Setup Checklist (new service)

### 1. pom.xml

```xml
<properties>
    <package.o.springdoc-openapi.version>2.8.17</package.o.springdoc-openapi.version>
</properties>

<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>${package.o.springdoc-openapi.version}</version>
</dependency>
```

### 2. application.yml (or via configfiles)

```yaml
springdoc.api-docs.path: /v3/api-docs
springdoc.swagger-ui.path: /swagger-ui.html
springdoc.swagger-ui.enabled: true
springdoc.api-docs.enabled: true
```

Permit Swagger paths without auth — add to `ISecurityConfiguration` exclusion list:
- `/swagger-ui.html`
- `/swagger-ui/**`
- `/v3/api-docs/**`

### 3. OpenApiConfig — create per service, customise title/tags

Security module pattern (use as template):

```java
@Configuration
public class OpenApiConfig {

    public static final String TAG_AUTH = "Authentication";

    @Bean
    public OpenAPI securityOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ikara Security API")
                        .description("Authentication and user management.")
                        .version("v1")
                        .contact(new Contact().name("Ikara Engineering").email("engineering@ikara.studio")));
    }
}
```

### 4. Controllers

```java
@Tag(name = OpenApiConfig.TAG_AUTH)
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Operation(summary = "Authenticate user", description = "Returns JWT on valid credentials.")
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody AuthenticationRequest req, HttpServletRequest request) { ... }
}
```

---

## Tag / Naming

- One tag per controller — matches functional domain
- Define as `TAG_*` constant in `OpenApiConfig`
- `@Tag(name = OpenApiConfig.TAG_*)` on controller class only
- `@Operation` takes only `summary` and `description` — never `tags`
