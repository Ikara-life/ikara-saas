package studio.ikara.commons.security.jwt;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serial;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Accessors(chain = true)
public class ContextAuthentication implements Authentication {

    @Serial
    private static final long serialVersionUID = 268979446038792690L;

    private ContextUser user;
    private boolean isAuthenticated;
    private String accessToken;
    private LocalDateTime accessTokenExpiryAt;

    @Override
    public String getName() {
        if (user == null) return null;
        return user.getFirstName();
    }

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (user == null) return List.of();
        return user.getAuthorities();
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.isAuthenticated = isAuthenticated;
    }

    @JsonIgnore
    @Override
    public Object getPrincipal() {
        return user;
    }
}
