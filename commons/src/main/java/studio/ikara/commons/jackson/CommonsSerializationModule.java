package studio.ikara.commons.jackson;

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.StdSerializer;
import java.io.Serial;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class CommonsSerializationModule extends SimpleModule {

    @Serial
    private static final long serialVersionUID = 3211716266446633817L;

    public CommonsSerializationModule() {

        super();

        this.addDeserializer(LocalDateTime.class, new StdDeserializer<>(LocalDateTime.class) {

            @Serial
            private static final long serialVersionUID = 7203629316456007849L;

            @Override
            public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {

                long inst = p.getValueAsLong();
                return LocalDateTime.ofEpochSecond(inst, 0, ZoneOffset.UTC);
            }
        });

        this.addSerializer(LocalDateTime.class, new StdSerializer<>(LocalDateTime.class) {

            @Serial
            private static final long serialVersionUID = 940937480894801043L;

            @Override
            public void serialize(LocalDateTime value, JsonGenerator gen, SerializationContext provider)
                    throws JacksonException {

                gen.writeNumber(value.toEpochSecond(ZoneOffset.UTC));
            }
        });
    }
}
