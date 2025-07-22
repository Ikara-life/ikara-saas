package studio.ikara.commons.jooq.util;

import java.math.BigInteger;
import org.jooq.types.ULong;

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
