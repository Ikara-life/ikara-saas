package studio.ikara.commons.jooq.util;

import org.jooq.types.ULong;

import java.math.BigInteger;

public class ULongUtil {

    private ULongUtil() {}

    public static ULong valueOf(Object o) {
        return switch (o) {
            case null -> null;
            case ULong v -> v;
            case BigInteger b -> ULong.valueOf(b);
            default -> ULong.valueOf(o.toString());
        };
    }
}
