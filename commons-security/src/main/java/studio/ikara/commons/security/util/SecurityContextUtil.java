package studio.ikara.commons.security.util;

import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import studio.ikara.commons.security.jwt.ContextAuthentication;
import studio.ikara.commons.security.jwt.ContextUser;

public class SecurityContextUtil {

    private static final String AUTHORITIES_PREFIX = "Authorities.";

    private SecurityContextUtil() {}

    public static CompletableFuture<Locale> getUsersLocale() {
        return getUsersContextUser().thenApply(user -> {
            String localeCode = user.getLocaleCode();
            return localeCode != null && !localeCode.isBlank() ? Locale.forLanguageTag(localeCode) : Locale.ENGLISH;
        });
    }

    public static CompletableFuture<ContextUser> getUsersContextUser() {
        return getUsersContextAuthentication().thenApply(ContextAuthentication::getUser);
    }

    public static CompletableFuture<ContextAuthentication> getUsersContextAuthentication() {
        return CompletableFuture.supplyAsync(() -> {
            SecurityContext context = SecurityContextHolder.getContext();
            return (ContextAuthentication) context.getAuthentication();
        });
    }

    public static CompletableFuture<Boolean> hasAuthority(String authority) {
        if (authority == null || authority.isBlank()) return CompletableFuture.completedFuture(true);

        String prefixedAuthority =
                authority.startsWith(AUTHORITIES_PREFIX) ? authority : AUTHORITIES_PREFIX + authority;

        return getUsersContextUser().thenApply(user -> hasAuthority(prefixedAuthority, user.getAuthorities()));
    }

    public static boolean hasAuthority(String authority, Collection<? extends GrantedAuthority> authorities) {
        if (authority == null || authority.isBlank()) return true;

        if (authorities == null || authorities.isEmpty()) return false;

        String prefixedAuthority =
                authority.startsWith(AUTHORITIES_PREFIX) ? authority : AUTHORITIES_PREFIX + authority;

        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals(prefixedAuthority));
    }
}
