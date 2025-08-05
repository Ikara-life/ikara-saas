package studio.ikara.commons.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;
import studio.ikara.commons.security.service.IAuthenticationService;
import studio.ikara.commons.security.util.ServerHttpRequestUtil;

@RequiredArgsConstructor
public class JWTTokenFilter extends OncePerRequestFilter {

    private final IAuthenticationService authService;

    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain)
            throws ServletException, IOException {

        String bearerToken = ServerHttpRequestUtil.extractBearerToken(request);

        if (bearerToken == null || bearerToken.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Authentication authentication =
                    authService.getAuthentication(bearerToken, request).join();

            if (authentication != null && authentication.isAuthenticated()) {
                saveContext(request, response, authentication);
                filterChain.doFilter(request, response);
            } else {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write("Invalid or expired token");
            }
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Authentication failed: " + e.getMessage());
        }
    }

    private void saveContext(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(context, request, response);
    }
}
