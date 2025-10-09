package studio.ikara.security.enums;
import org.jooq.EnumType;
import org.jooq.Schema;
import studio.ikara.security.jooq.public_.Public;

public enum CoachTypeCode implements EnumType {
    INDIVIDUAL("INDIVIDUAL"),
    ENTITY("ENTITY");

    private final String literal;

    CoachTypeCode(String literal) {
        this.literal = literal;
    }

    public static CoachTypeCode lookupLiteral(String literal) {
        return EnumType.lookupLiteral(CoachTypeCode.class, literal);
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
        return "security_coach_type_code";
    }
}
