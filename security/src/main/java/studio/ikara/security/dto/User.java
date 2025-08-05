package studio.ikara.security.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serial;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import studio.ikara.commons.model.dto.AbstractUpdatableDTO;
import studio.ikara.commons.security.jwt.ContextUser;
import studio.ikara.security.enums.UserStatusCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ToString(callSuper = true)
public class User extends AbstractUpdatableDTO<Long, Long> {

    public static final String PLACEHOLDER = "NONE";

    @Serial
    private static final long serialVersionUID = 4974016469017457972L;

    private String userName;
    private String emailId;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String middleName;
    private String localeCode;
    private String password;
    private boolean passwordHashed = Boolean.TRUE;
    private UserStatusCode userStatusCode;
    private Short noFailedAttempt;
    private Set<String> authorities;

    public String getUserName() {
        return PLACEHOLDER.equals(this.userName) ? null : this.userName;
    }

    public String getEmailId() {
        return PLACEHOLDER.equals(this.emailId) ? null : this.emailId;
    }

    public String getPhoneNumber() {
        return PLACEHOLDER.equals(this.phoneNumber) ? null : this.phoneNumber;
    }

    public boolean checkIdentificationKeys() {
        return (this.userName == null || PLACEHOLDER.equals(this.userName))
                && (this.emailId == null || PLACEHOLDER.equals(this.emailId))
                && (this.phoneNumber == null || PLACEHOLDER.equals(this.phoneNumber));
    }

    @JsonIgnore
    public String getPassword() {
        return this.password;
    }

    @JsonIgnore
    public boolean isPasswordHashed() {
        return this.passwordHashed;
    }

    public User setAuthorities(Collection<String> authorities) {
        this.authorities = authorities != null ? Set.copyOf(authorities) : null;
        return this;
    }

    @JsonIgnore
    public ContextUser toContextUser() {
        return new ContextUser()
                .setId(getId())
                .setCreatedBy(getCreatedBy())
                .setUpdatedBy(getUpdatedBy())
                .setCreatedAt(getCreatedAt())
                .setUpdatedAt(getUpdatedAt())
                .setUserName(getUserName())
                .setEmailId(getEmailId())
                .setPhoneNumber(getPhoneNumber())
                .setFirstName(getFirstName())
                .setLastName(getLastName())
                .setMiddleName(getMiddleName())
                .setLocaleCode(getLocaleCode())
                .setPassword(getPassword())
                .setPasswordHashed(isPasswordHashed())
                .setNoFailedAttempt(getNoFailedAttempt())
                .setStringAuthorities(authorities != null ? List.copyOf(authorities) : null);
    }
}
