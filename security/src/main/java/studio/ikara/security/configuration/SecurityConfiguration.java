package studio.ikara.security.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import studio.ikara.commons.jooq.configuration.AbstractJooqBaseConfiguration;
import studio.ikara.commons.security.ISecurityConfiguration;
import studio.ikara.commons.security.service.IAuthenticationService;
import studio.ikara.security.service.SecurityMessageResourceService;

@Configuration
public class SecurityConfiguration extends AbstractJooqBaseConfiguration implements ISecurityConfiguration {

    protected SecurityMessageResourceService messageResourceService;
    private final IAuthenticationService authenticationService;

    public SecurityConfiguration(
            SecurityMessageResourceService messageResourceService,
            ObjectMapper objectMapper,
            @Lazy IAuthenticationService authenticationService) {
        super(objectMapper);
        this.messageResourceService = messageResourceService;
        this.authenticationService = authenticationService;
    }

    @Override
    @PostConstruct
    public void initialize() {
        super.initialize(messageResourceService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return springSecurityFilterChain(
                http,
                authenticationService,
                objectMapper,
                "/api/v1/auth/**",
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/v3/api-docs/**");
    }
}
