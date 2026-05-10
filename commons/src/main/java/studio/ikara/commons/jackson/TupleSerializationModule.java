package studio.ikara.commons.jackson;

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.node.ArrayNode;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import studio.ikara.commons.function.Tuple2;
import studio.ikara.commons.function.Tuple3;
import studio.ikara.commons.function.Tuple4;
import studio.ikara.commons.function.Tuple5;
import studio.ikara.commons.function.Tuple6;
import studio.ikara.commons.function.Tuple7;
import studio.ikara.commons.function.Tuple8;
import studio.ikara.commons.function.Tuples;

public class TupleSerializationModule extends SimpleModule {

    @Serial
    private static final long serialVersionUID = 380051999564048056L;

    @SuppressWarnings("rawtypes")
    public TupleSerializationModule() {

        super();

        this.addSerializer(Tuple2.class, new ValueSerializer<Tuple2>() {

            @Override
            public void serialize(Tuple2 value, JsonGenerator gen, SerializationContext serializers) throws JacksonException {
                writeTuple(value, gen);
            }
        });

        this.addSerializer(Tuple3.class, new ValueSerializer<>() {

            @Override
            public void serialize(Tuple3 value, JsonGenerator gen, SerializationContext serializers) throws JacksonException {
                writeTuple(value, gen);
            }
        });

        this.addSerializer(Tuple4.class, new ValueSerializer<Tuple4>() {

            @Override
            public void serialize(Tuple4 value, JsonGenerator gen, SerializationContext serializers) throws JacksonException {
                writeTuple(value, gen);
            }
        });

        this.addSerializer(Tuple5.class, new ValueSerializer<Tuple5>() {

            @Override
            public void serialize(Tuple5 value, JsonGenerator gen, SerializationContext serializers) throws JacksonException {
                writeTuple(value, gen);
            }
        });

        this.addSerializer(Tuple6.class, new ValueSerializer<Tuple6>() {

            @Override
            public void serialize(Tuple6 value, JsonGenerator gen, SerializationContext serializers) throws JacksonException {
                writeTuple(value, gen);
            }
        });

        this.addSerializer(Tuple7.class, new ValueSerializer<Tuple7>() {

            @Override
            public void serialize(Tuple7 value, JsonGenerator gen, SerializationContext serializers) throws JacksonException {
                writeTuple(value, gen);
            }
        });

        this.addSerializer(Tuple8.class, new ValueSerializer<Tuple8>() {

            @Override
            public void serialize(Tuple8 value, JsonGenerator gen, SerializationContext serializers) throws JacksonException {
                writeTuple(value, gen);
            }
        });

        this.addDeserializer(Tuple2.class, new ValueDeserializer<Tuple2>() {

            @Override
            public Tuple2 deserialize(JsonParser jp, DeserializationContext ctxt) throws JacksonException {

                JsonNode node = ctxt.readTree(jp);
                List<Object> result = new ArrayList<>();
                if (node != null) {
                    if (node instanceof ArrayNode arrayNode) {
                        for (var elementNode : arrayNode) {
                            result.add(ctxt.readValue(elementNode.traverse(ctxt), Object.class));
                        }
                    } else {
                        result.add(ctxt.readValue(node.traverse(ctxt), Object.class));
                    }
                }

                if (result.size() < 2 || result.size() > 8)
                    throw new tools.jackson.core.exc.StreamReadException(jp,
                            "Tuple can have min 2 and max 8 elements but found : " + result.size() + " elements");

                return Tuples.fromArray(result.toArray());
            }
        });
    }

    @SuppressWarnings("rawtypes")
    private static void writeTuple(Tuple2 value, JsonGenerator gen) throws JacksonException {
        gen.writeStartArray();
        for (Object e : value.toArray()) {
            gen.writePOJO(e);
        }
        gen.writeEndArray();
    }
}
