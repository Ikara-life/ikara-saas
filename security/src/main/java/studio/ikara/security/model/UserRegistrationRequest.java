package studio.ikara.security.model;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import studio.ikara.security.dto.User;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class UserRegistrationRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 3408129291756353439L;

    private String password;
    private String emailId;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String middleName;
    private String localeCode;

    public User toUser() {
        return new User()
                .setUserName(emailId)
                .setPassword(password)
                .setEmailId(emailId)
                .setPhoneNumber(phoneNumber)
                .setFirstName(firstName)
                .setLastName(lastName)
                .setMiddleName(middleName)
                .setLocaleCode(localeCode);
    }
}
