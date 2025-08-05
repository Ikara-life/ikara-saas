package studio.ikara.security.enums;

import org.jooq.EnumType;
import org.jooq.Schema;
import studio.ikara.security.jooq.public_.Public;

public enum UserStatusCode implements EnumType {
    ACTIVE("ACTIVE"),

    INACTIVE("INACTIVE"),

    DELETED("DELETED"),

    LOCKED("LOCKED"),

    PASSWORD_EXPIRED("PASSWORD_EXPIRED");

    private final String literal;

    UserStatusCode(String literal) {
        this.literal = literal;
    }

    public static UserStatusCode lookupLiteral(String literal) {
        return EnumType.lookupLiteral(UserStatusCode.class, literal);
    }

    @Override
    public String getLiteral() {
        return this.literal;
    }

    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public String getName() {
        return "security_user_status_code";
    }

    public Boolean isActive() {
        return this == ACTIVE;
    }

    public Boolean isInActive() {
        return this == INACTIVE || this == DELETED || this == LOCKED || this == PASSWORD_EXPIRED;
    }
}
