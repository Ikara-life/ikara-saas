package studio.ikara.commons.jooq.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;
import org.jooq.types.UShort;
import studio.ikara.commons.configuration.AbstractBaseConfiguration;
import studio.ikara.commons.configuration.service.AbstractMessageService;
import lombok.Getter;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import studio.ikara.commons.jooq.gson.UNumberAdapter;
import studio.ikara.commons.jooq.gson.UNumberListAdapter;
import studio.ikara.commons.jooq.jackson.UnsignedNumbersSerializationModule;

import java.util.List;

@Getter
public abstract class AbstractJooqBaseConfiguration extends AbstractBaseConfiguration {

    @Value("${spring.datasource.url}")
    protected String url;

    @Value("${spring.datasource.username}")
    protected String username;

    @Value("${spring.datasource.password}")
    protected String password;

    protected AbstractJooqBaseConfiguration(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    public void initialize(AbstractMessageService messageResourceService) {
        super.initialize();
        this.objectMapper.registerModule(new UnsignedNumbersSerializationModule(messageResourceService));
    }

    @Override
    public Gson makeGson() {
        return super.makeGson()
                .newBuilder()
                .registerTypeAdapter(ULong.class, new UNumberAdapter<>(ULong.class))
                .registerTypeAdapter(new TypeToken<List<ULong>>() {}.getType(), new UNumberListAdapter<>(ULong.class))
                .registerTypeAdapter(UInteger.class, new UNumberAdapter<>(UInteger.class))
                .registerTypeAdapter(
                        new TypeToken<List<UInteger>>() {}.getType(), new UNumberListAdapter<>(UInteger.class))
                .registerTypeAdapter(UShort.class, new UNumberAdapter<>(UShort.class))
                .registerTypeAdapter(new TypeToken<List<UShort>>() {}.getType(), new UNumberListAdapter<>(UShort.class))
                .create();
    }

    @Bean
    public DSLContext context() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        TransactionAwareDataSourceProxy proxy = new TransactionAwareDataSourceProxy(dataSource);

        return DSL.using(proxy, SQLDialect.POSTGRES);
    }
}
