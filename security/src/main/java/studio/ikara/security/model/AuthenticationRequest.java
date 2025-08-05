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
public class AuthenticationRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 970479628689715542L;

    private String userName;
    private String password;
    private boolean rememberMe;

    public static AuthenticationRequest of(String userName, String password, boolean rememberMe) {
        return new AuthenticationRequest()
                .setUserName(userName)
                .setPassword(password)
                .setRememberMe(rememberMe);
    }

    public static AuthenticationRequest of(String userName, String password) {
        return of(userName, password, false);
    }

    public static AuthenticationRequest of(User user) {
        return of(user.getUserName(), user.getPassword());
    }
}
