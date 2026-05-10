package studio.ikara.commons.jooq.jackson;

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;
import org.jooq.types.UNumber;

public class UNumberSerializer extends StdSerializer<UNumber> {

    private static final long serialVersionUID = -2888640386444756529L;

    public UNumberSerializer() {
        super(UNumber.class);
    }

    @Override
    public void serialize(UNumber value, JsonGenerator gen, SerializationContext provider) throws JacksonException {

        gen.writeNumber(value.toBigInteger());
    }
}
