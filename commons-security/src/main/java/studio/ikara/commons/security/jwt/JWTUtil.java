package studio.ikara.commons.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JWTUtil {

    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final long EXPIRATION_MILLIS = 3600_000;

    public String generateToken(ContextUser contextUser, String hostName, String port) {
        Map<String, Object> claims = Map.ofEntries(
                Map.entry("userId", contextUser.getId()),
                Map.entry("hostName", hostName),
                Map.entry("port", port),
                Map.entry("isCoach", contextUser.isCoach()),
                Map.entry("coachId", contextUser.getCoachId())
        );

        Instant now = Instant.now();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(EXPIRATION_MILLIS)))
                .signWith(key)
                .compact();
    }

    public Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }

    public boolean isTokenValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public JWTClaims extractClaims(String token) {
        try {
            Jws<Claims> parsed = parseToken(token);
            return JWTClaims.from(parsed);
        } catch (JwtException e) {
            log.error("Failed to extract claims from JWT: {}", e.getMessage());
            return null;
        }
    }
}