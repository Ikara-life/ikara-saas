package studio.ikara.commons.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.io.Serial;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class CommonsSerializationModule extends SimpleModule {

    @Serial
    private static final long serialVersionUID = 3211716266446633817L;

    public CommonsSerializationModule() {

        super();

        this.addDeserializer(LocalDateTime.class, new StdDeserializer<>((Class<?>) null) {

            @Serial
            private static final long serialVersionUID = 7203629316456007849L;

            @Override
            public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

                long inst = p.getValueAsLong();
                return LocalDateTime.ofEpochSecond(inst, 0, ZoneOffset.UTC);
            }
        });

        this.addSerializer(LocalDateTime.class, new StdSerializer<>((Class<LocalDateTime>) null) {

            @Serial
            private static final long serialVersionUID = 940937480894801043L;

            @Override
            public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider provider)
                    throws IOException {

                gen.writeNumber(value.toEpochSecond(ZoneOffset.UTC));
            }
        });
    }
}
