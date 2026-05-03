# OpenAPI / Swagger Setup

Reusable pattern for ikara Spring Boot 3.5.3 services.

`springdoc-openapi-starter-webmvc-ui` 2.8.9. Spring Boot 3.x — NOT old `springdoc-openapi-ui` 1.x (that Spring Boot 2.7).

## Endpoints (security service, port 8001)

| Purpose | URL |
|---|---|
| Swagger UI (all groups) | `http://localhost:8001/swagger-ui.html` |
| External JSON | `http://localhost:8001/v3/api-docs/external` |
| Backoffice JSON | `http://localhost:8001/v3/api-docs/backoffice` |
| Internal JSON | `http://localhost:8001/v3/api-docs/internal` |

## Endpoints (core service, port 8005)

| Purpose | URL |
|---|---|
| Swagger UI (all groups) | `http://localhost:8005/swagger-ui.html` |
| External JSON | `http://localhost:8005/v3/api-docs/external` |
| Backoffice JSON | `http://localhost:8005/v3/api-docs/backoffice` |
| Internal JSON | `http://localhost:8005/v3/api-docs/internal` |

Postman import: **Import → Link → paste URL**.

---

## Setup Checklist (new service)

### 1. pom.xml

```xml
<properties>
    <package.o.springdoc-openapi.version>2.8.9</package.o.springdoc-openapi.version>
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

Permit Swagger paths without auth — add to `ISecurityConfiguration` permitted matchers:
- `/swagger-ui.html`
- `/swagger-ui/**`
- `/v3/api-docs/**`

### 3. Annotation package — create once per service

`annotation/Visibility.java`:
```java
public enum Visibility { EXTERNAL, BACKOFFICE, INTERNAL }
```

`annotation/ApiVisibility.java`:
```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiVisibility {
    Visibility[] value();
}
```

### 4. OpenApiConfig — create per service, customise title/tags

```java
@Configuration
public class OpenApiConfig {

    // One constant per controller. Referenced via @Tag(name=) on the controller class.
    public static final String TAG_EXAMPLE = "Example";

    @Bean
    public OpenAPI serviceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ikara Security API")
                        .description("Authentication and user management.")
                        .version("v1")
                        .contact(new Contact().name("Ikara Engineering").email("engineering@ikara.studio")));
    }

    @Bean
    public GroupedOpenApi externalApiGroup() {
        return GroupedOpenApi.builder()
                .group("external")
                .addOpenApiMethodFilter(method -> hasVisibility(method, Visibility.EXTERNAL))
                .build();
    }

    @Bean
    public GroupedOpenApi backofficeApiGroup() {
        return GroupedOpenApi.builder()
                .group("backoffice")
                .addOpenApiMethodFilter(method -> hasVisibility(method, Visibility.BACKOFFICE))
                .build();
    }

    @Bean
    public GroupedOpenApi internalApiGroup() {
        return GroupedOpenApi.builder()
                .group("internal")
                .pathsToMatch("/**")
                .build();
    }

    private static boolean hasVisibility(Method method, Visibility... allowedTiers) {
        Set<Visibility> allowed = Set.of(allowedTiers);
        ApiVisibility ann = method.getAnnotation(ApiVisibility.class);
        if (ann == null) ann = method.getDeclaringClass().getAnnotation(ApiVisibility.class);
        if (ann == null) return allowed.contains(Visibility.INTERNAL);
        return Arrays.stream(ann.value()).anyMatch(allowed::contains);
    }
}
```

### 5. Controllers

```java
@Tag(name = OpenApiConfig.TAG_EXAMPLE)
@ApiVisibility(Visibility.INTERNAL)    // default — override per method
@RestController
@RequestMapping("/api/v1/example")
public class ExampleController {

    @ApiVisibility(Visibility.EXTERNAL)
    @Operation(summary = "Get example", description = "Returns example resource.")
    @GetMapping("/{id}")
    public ResponseEntity<ExampleDTO> get(@PathVariable Long id) { ... }

    // No annotation — inherits INTERNAL from class
    @Operation(summary = "Internal admin endpoint")
    @GetMapping("/admin")
    public ResponseEntity<ExampleDTO> admin() { ... }
}
```

---

## Access Tier Rules

| Tier | Annotation | Visible in groups |
|---|---|---|
| External | `@ApiVisibility(EXTERNAL)` | external, internal |
| Backoffice | `@ApiVisibility(BACKOFFICE)` | backoffice, internal |
| Internal | `@ApiVisibility(INTERNAL)` or none | internal only |

Default INTERNAL. Put `@ApiVisibility(INTERNAL)` on controller class — methods inherit.

---

## Tag / Folder Naming

No controllers yet. When adding to security or core service:
- One tag per controller — matches functional domain
- Define as `TAG_*` constant in `OpenApiConfig`
- `@Tag(name = OpenApiConfig.TAG_*)` on controller class only
- `@Operation` takes only `summary` and `description` — never `tags`

---

## Adding New Tier

1. Add value to `Visibility` enum
2. Add `GroupedOpenApi` bean in `OpenApiConfig` with `hasVisibility(method, ...)` filter
3. Add `TAG_*` constant for new controller
4. Annotate controller class with `@Tag` and `@ApiVisibility`