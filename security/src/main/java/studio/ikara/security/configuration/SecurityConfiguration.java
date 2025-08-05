package studio.ikara.security.configuration;

import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import studio.ikara.commons.jooq.configuration.AbstractJooqBaseConfiguration;
import studio.ikara.commons.security.ISecurityConfiguration;
import studio.ikara.security.service.SecurityMessageResourceService;

@Configuration
public class SecurityConfiguration extends AbstractJooqBaseConfiguration
		implements ISecurityConfiguration {

	protected SecurityMessageResourceService messageResourceService;

	public SecurityConfiguration(SecurityMessageResourceService messageResourceService, ObjectMapper objectMapper) {
		super(objectMapper);
		this.messageResourceService = messageResourceService;
	}

	@Override
	@PostConstruct
	public void initialize() {
		super.initialize(messageResourceService);
	}

}
