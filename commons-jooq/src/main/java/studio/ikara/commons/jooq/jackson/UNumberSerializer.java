package studio.ikara.commons.jooq.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import org.jooq.types.UNumber;

public class UNumberSerializer extends StdSerializer<UNumber> {

    private static final long serialVersionUID = -2888640386444756529L;

    public UNumberSerializer() {
        super(UNumber.class);
    }

    @Override
    public void serialize(UNumber value, JsonGenerator gen, SerializerProvider provider) throws IOException {

        gen.writeNumber(value.toBigInteger());
    }
}
