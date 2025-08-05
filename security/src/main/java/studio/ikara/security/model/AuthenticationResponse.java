package studio.ikara.security.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import studio.ikara.commons.security.jwt.ContextUser;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class AuthenticationResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 8718463183001968129L;

    private ContextUser user;
    private String accessToken;
    private LocalDateTime accessTokenExpiryAt;
}
