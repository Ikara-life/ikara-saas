package studio.ikara.commons.jooq.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import studio.ikara.commons.configuration.service.AbstractMessageService;
import studio.ikara.commons.exception.GenericException;
import org.jooq.types.UNumber;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.Serial;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class UNumberDeserializer<R extends UNumber> extends StdDeserializer<R> {

    @Serial
    private static final long serialVersionUID = 9146228421680912993L;

    private final Class<R> classs;

    private final transient Method method;

    private transient AbstractMessageService msgResource;

    public UNumberDeserializer(Class<R> classs, AbstractMessageService msgResource) {
        super((Class<?>) null);
        this.classs = classs;
        try {
            this.method = this.classs.getDeclaredMethod("valueOf", String.class);
        } catch (Exception e) {
            throw new GenericException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    msgResource.getDefaultLocaleMessage(AbstractMessageService.VALUEOF_METHOD_NOT_FOUND));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public R deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

        String str = p.getValueAsString();

        try {
            return (R) this.method.invoke(null, str);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new GenericException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    msgResource.getDefaultLocaleMessage(
                            AbstractMessageService.UNABLE_TO_CONVERT, str, this.classs.getSimpleName()));
        }
    }
}
