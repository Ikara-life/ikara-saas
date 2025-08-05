package studio.ikara.security.service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import studio.ikara.commons.exception.GenericException;
import studio.ikara.commons.function.Tuple2;
import studio.ikara.commons.security.jwt.ContextAuthentication;
import studio.ikara.commons.security.jwt.ContextUser;
import studio.ikara.commons.security.jwt.JWTClaims;
import studio.ikara.commons.security.jwt.JWTUtil;
import studio.ikara.commons.security.service.IAuthenticationService;
import studio.ikara.commons.service.CacheService;
import studio.ikara.security.model.AuthenticationRequest;
import studio.ikara.security.model.AuthenticationResponse;

@Service
public class AuthenticationService implements IAuthenticationService {

    private final UserService userService;

    private final CacheService cacheService;

    @Value("${jwt.key:defaultSecretKey}")
    private String tokenKey;

    @Value("${jwt.token.rememberme.expiry:1440}")
    private Integer rememberMeExpiryInMinutes;

    @Value("${jwt.token.default.expiry:60}")
    private Integer defaultExpiryInMinutes;

    public AuthenticationService(UserService userService, CacheService cacheService) {
        this.userService = userService;
        this.cacheService = cacheService;
    }

    public CompletableFuture<AuthenticationResponse> authenticate(
            AuthenticationRequest authRequest, HttpServletRequest request) {

        return userService.findByUsername(authRequest.getUserName()).thenComposeAsync(user -> {
            if (user == null) throw new GenericException(HttpStatus.UNAUTHORIZED, "Invalid credentials");

            if (user.getUserStatusCode().isInActive())
                throw new GenericException(HttpStatus.UNAUTHORIZED, "User account is disabled");

            return userService.validatePassword(user, authRequest.getPassword()).thenComposeAsync(isValid -> {
                if (Boolean.FALSE.equals(isValid))
                    throw new GenericException(HttpStatus.UNAUTHORIZED, "Invalid credentials");

                return userService
                        .toContextUser(user)
                        .thenComposeAsync(
                                contextUser -> generateToken(contextUser, authRequest.isRememberMe(), request));
            });
        });
    }

    private CompletableFuture<AuthenticationResponse> generateToken(
            ContextUser user, boolean rememberMe, HttpServletRequest request) {
        int timeInMinutes = rememberMe ? rememberMeExpiryInMinutes : defaultExpiryInMinutes;

        String host = request.getRemoteHost();
        String port = "" + request.getLocalPort();

        Tuple2<String, LocalDateTime> token = JWTUtil.generateToken(JWTUtil.JWTGenerateTokenParameters.builder()
                .userId(user.getId())
                .secretKey(tokenKey)
                .expiryInMin(timeInMinutes)
                .host(host)
                .port(port)
                .build());

        return CompletableFuture.completedFuture(new AuthenticationResponse()
                .setUser(user)
                .setAccessToken(token.getT1())
                .setAccessTokenExpiryAt(token.getT2()));
    }

    @Override
    public CompletableFuture<Authentication> getAuthentication(String bearerToken, HttpServletRequest request) {
        if (bearerToken == null || bearerToken.isBlank())
            return CompletableFuture.completedFuture(new ContextAuthentication(null, false, null, null));

        bearerToken = bearerToken.trim();

        if (bearerToken.startsWith("Bearer ")) bearerToken = bearerToken.substring(7);

        final String token = bearerToken;

        return extractAndValidateToken(token, request);
    }

    private CompletableFuture<Authentication> extractAndValidateToken(String token, HttpServletRequest request) {
        try {
            JWTClaims claims = JWTUtil.getClaimsFromToken(tokenKey, token);

            String host = request.getRemoteHost();
            if (!host.equals(claims.getHostName()))
                return CompletableFuture.completedFuture(new ContextAuthentication(null, false, null, null));

            return userService.read(claims.getUserId()).thenComposeAsync(user -> {
                if (user == null)
                    return CompletableFuture.completedFuture(new ContextAuthentication(null, false, null, null));

                if (user.getUserStatusCode().isInActive())
                    return CompletableFuture.completedFuture(new ContextAuthentication(null, false, null, null));

                return userService.toContextUser(user).thenComposeAsync(contextUser -> {
                    ContextAuthentication auth = new ContextAuthentication(
                            contextUser,
                            true,
                            token,
                            LocalDateTime.now(ZoneId.of("UTC")).plusMinutes(defaultExpiryInMinutes));

                    return CompletableFuture.completedFuture(auth);
                });
            });
        } catch (Exception e) {
            return CompletableFuture.completedFuture(new ContextAuthentication(null, false, null, null));
        }
    }
}
