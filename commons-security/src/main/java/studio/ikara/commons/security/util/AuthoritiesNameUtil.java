package studio.ikara.commons.security.util;

public class AuthoritiesNameUtil {

    private AuthoritiesNameUtil() {}

    /**
     * Generates a role authority string.
     * Format: {@code Authorities.{CLIENT_CODE}.ROLE_{NAME}}
     * Example: {@code Authorities.DEMO_STUDIO.ROLE_ADMIN}
     */
    public static String makeRoleName(String clientCode, String roleName) {
        StringBuilder sb = new StringBuilder("Authorities.");
        if (clientCode != null) sb.append(clientCode.toUpperCase()).append('.');
        sb.append("ROLE_").append(roleName.replace(' ', '_').toUpperCase());
        return sb.toString();
    }

    /**
     * Generates a permission authority string.
     * Format: {@code Authorities.{CLIENT_CODE}.{PERMISSION_CODE}}
     * Example: {@code Authorities.DEMO_STUDIO.USER_CREATE}
     */
    public static String makePermissionName(String clientCode, String permissionCode) {
        StringBuilder sb = new StringBuilder("Authorities.");
        if (clientCode != null) sb.append(clientCode.toUpperCase()).append('.');
        sb.append(permissionCode.replace(' ', '_').toUpperCase());
        return sb.toString();
    }
}
