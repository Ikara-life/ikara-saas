package studio.ikara.security.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
                        .contact(new Contact()
                                .name("Ikara Engineering")
                                .email("engineering@ikara.studio")));
    }
}
