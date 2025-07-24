package studio.ikara.commons.security.jwt;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Data
@Accessors(chain = true)
@ToString
public class ContextUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 3430294045916132045L;

    private Long id;
    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String userName;
    private String emailId;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String middleName;
    private String localeCode;
    private String password;
    private boolean passwordHashed;
    private Short noFailedAttempt;
    private String statusCode;
    private List<String> stringAuthorities;

    @JsonIgnore
    private Set<SimpleGrantedAuthority> grantedAuthorities;

    @JsonIgnore
    public Collection<SimpleGrantedAuthority> getAuthorities() {

        if (this.stringAuthorities == null || this.stringAuthorities.isEmpty()) return Set.of();

        if (this.grantedAuthorities == null)
            this.grantedAuthorities = this.stringAuthorities.parallelStream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet());

        return this.grantedAuthorities;
    }

    @JsonIgnore
    public String getPassword() {
        return this.password;
    }

    @JsonIgnore
    public boolean isPasswordHashed() {
        return this.passwordHashed;
    }
}
