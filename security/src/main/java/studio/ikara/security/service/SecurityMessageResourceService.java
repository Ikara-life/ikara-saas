package studio.ikara.security.service;

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.springframework.stereotype.Service;

import studio.ikara.commons.configuration.service.AbstractMessageService;

@Service
public class SecurityMessageResourceService extends AbstractMessageService {

	public SecurityMessageResourceService() {
		super(Map.of(Locale.ENGLISH, ResourceBundle.getBundle("messages", Locale.ENGLISH)));
	}



}
