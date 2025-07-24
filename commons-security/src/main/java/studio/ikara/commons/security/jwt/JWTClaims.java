package studio.ikara.commons.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ToString
public class JWTClaims implements Serializable {

    private static final String ONE_TIME = "oneTime";

    @Serial
    private static final long serialVersionUID = 4106423186808388123L;

    private Long userId;
    private String hostName;
    private String port;
    private boolean oneTime = false;

    public static JWTClaims from(Jws<Claims> parsed) {

        Claims claims = parsed.getBody();

        return new JWTClaims()
                .setUserId(claims.get("userId", Long.class))
                .setHostName(claims.get("hostName", String.class))
                .setPort(claims.get("port", String.class))
                .setOneTime(claims.containsKey(ONE_TIME) ? claims.get(ONE_TIME, Boolean.class) : Boolean.FALSE);
    }

    public Map<String, Object> getClaimsMap() {

        Map<String, Object> map = new HashMap<>();

        map.put("userId", this.userId);
        map.put("hostName", this.hostName);
        map.put("port", this.port);

        map.put(ONE_TIME, this.oneTime);

        return map;
    }
}
