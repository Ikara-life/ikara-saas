package studio.ikara.commons.security.service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;
import org.springframework.security.core.Authentication;

public interface IAuthenticationService {

    String CACHE_NAME_TOKEN = "tokenCache";

    CompletableFuture<Authentication> getAuthentication(String bearerToken, HttpServletRequest request);
}
