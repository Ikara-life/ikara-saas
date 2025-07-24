package studio.ikara.commons.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import lombok.Builder;
import studio.ikara.commons.function.Tuple2;
import studio.ikara.commons.function.Tuples;

public class JWTUtil {

    private JWTUtil() {}

    public static Tuple2<String, LocalDateTime> generateToken(JWTGenerateTokenParameters params) {

        LocalDateTime expirationTime = LocalDateTime.now(ZoneId.of("UTC")).plusMinutes(params.expiryInMin);

        return Tuples.of(
                Jwts.builder()
                        .issuer("aplygen")
                        .subject(params.userId.toString())
                        .claims(new JWTClaims()
                                .setUserId(params.userId)
                                .setHostName(params.host)
                                .setPort(params.port)
                                .setOneTime(params.oneTime)
                                .getClaimsMap())
                        .issuedAt(Date.from(Instant.now()))
                        .expiration(Date.from(Instant.now().plus(params.expiryInMin, ChronoUnit.MINUTES)))
                        .signWith(Keys.hmacShaKeyFor(params.secretKey.getBytes()), Jwts.SIG.HS512)
                        .compact(),
                expirationTime);
    }

    public static JWTClaims getClaimsFromToken(String secretKey, String token) {

        JwtParser parser = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build();

        Jws<Claims> parsed = parser.parseSignedClaims(token);

        return JWTClaims.from(parsed);
    }

    @Builder
    public static class JWTGenerateTokenParameters {

        Long userId;
        String secretKey;
        Integer expiryInMin;
        String host;
        String port;

        @Builder.Default
        boolean oneTime = false;
    }
}
