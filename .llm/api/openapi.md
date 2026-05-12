# OpenAPI Setup

springdoc `3.0.3` for Spring Boot 4.x. Artifact: `springdoc-openapi-starter-webmvc-ui:3.0.3`.

## Endpoints

| Service | OpenAPI JSON | UI |
|---|---|---|
| security (8001) | `http://localhost:8001/v3/api-docs` | `http://localhost:8001/swagger-ui.html` |
| core (8005) | `http://localhost:8005/v3/api-docs` | `http://localhost:8005/swagger-ui.html` |

Postman import: **Import → Link → paste `/v3/api-docs` URL**.

---

## Known Fix — Base64 Response Bug

springdoc 3.x returns spec as `byte[]` with `application/json`. Jackson 3.x (in `@Bean JacksonJsonHttpMessageConverter`) base64-encodes byte arrays. Fix already applied in `AbstractBaseConfiguration.extendMessageConverters()` — moves `ByteArrayHttpMessageConverter` before Jackson in converter chain. No action needed for new services; fix lives in commons.

---

## Setup Checklist (new service)

### 1. pom.xml

```xml
<properties>
    <package.o.springdoc.version>3.0.3</package.o.springdoc.version>
</properties>

<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>${package.o.springdoc.version}</version>
</dependency>
```

### 2. configfiles/<service>.yml

```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
```

No need to set `url`, `config-url`, or `disable-swagger-default-url` — those cause rendering issues.

Permit paths in `SecurityFilterChain` (already in `ISecurityConfiguration` default):
- `/swagger-ui/**`, `/swagger-ui.html`, `/v3/api-docs`, `/v3/api-docs/**`

### 3. OpenApiConfig — one per service

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

    @Operation(summary = "Login", description = "Returns JWT on valid credentials.")
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody AuthenticationRequest req, HttpServletRequest request) { ... }
}
```

---

## Tag / Naming Rules

- One tag per controller — matches functional domain
- Define as `TAG_*` constant in `OpenApiConfig`
- `@Tag(name = OpenApiConfig.TAG_*)` on controller class only, never on methods
- `@Operation` takes only `summary` + `description` — no `tags`, no `operationId` override
