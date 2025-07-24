package studio.ikara.commons.security.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;

public class ServerHttpRequestUtil {

    private ServerHttpRequestUtil() {}

    public static String extractBearerToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);

        if ((bearerToken == null || bearerToken.isBlank()) && request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (HttpHeaders.AUTHORIZATION.equalsIgnoreCase(cookie.getName())) {
                    bearerToken = cookie.getValue();
                    break;
                }
            }
        }

        if (bearerToken != null) {
            bearerToken = bearerToken.trim();
            if (bearerToken.regionMatches(true, 0, "Bearer ", 0, 7)) return bearerToken.substring(7);
        }

        return null;
    }
}
