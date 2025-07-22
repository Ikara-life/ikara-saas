package studio.ikara.commons.jooq.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.Serial;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;
import org.jooq.types.UShort;
import studio.ikara.commons.configuration.service.AbstractMessageService;

public class UnsignedNumbersSerializationModule extends SimpleModule {

    @Serial
    private static final long serialVersionUID = 6367988430700197837L;

    public UnsignedNumbersSerializationModule(AbstractMessageService messageResourceService) {
        super();

        this.addDeserializer(ULong.class, new UNumberDeserializer<>(ULong.class, messageResourceService));
        this.addDeserializer(UShort.class, new UNumberDeserializer<>(UShort.class, messageResourceService));
        this.addDeserializer(UInteger.class, new UNumberDeserializer<>(UInteger.class, messageResourceService));
        this.addSerializer(new UNumberSerializer());
    }
}
