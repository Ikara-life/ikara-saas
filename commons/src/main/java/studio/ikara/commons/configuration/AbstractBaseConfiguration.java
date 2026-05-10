package studio.ikara.commons.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import jakarta.annotation.PostConstruct;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import studio.ikara.commons.codec.RedisJSONCodec;
import studio.ikara.commons.codec.RedisObjectCodec;
import studio.ikara.commons.gson.LocalDateTimeAdapter;
import studio.ikara.commons.jackson.CommonsSerializationModule;

public abstract class AbstractBaseConfiguration implements WebMvcConfigurer {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractBaseConfiguration.class);

    protected JsonMapper objectMapper;

    @Value("${redis.url:}")
    private String redisURL;

    @Value("${redis.codec:object}")
    private String codecType;

    private RedisCodec<String, Object> objectCodec;

    protected AbstractBaseConfiguration(JsonMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    protected void initialize() {
        this.objectMapper = this.objectMapper.rebuild()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .changeDefaultPropertyInclusion(ignored -> JsonInclude.Value.construct(Include.NON_EMPTY, Include.ALWAYS))
                .addModule(new CommonsSerializationModule())
                .build();
        this.objectCodec = "object".equals(codecType) ? new RedisObjectCodec() : new RedisJSONCodec(this.objectMapper);
    }

    @PostConstruct
    public void enableAuthCtxOnSpawnedThreads() {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    @Bean
    public Gson makeGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    @Bean
    public JacksonJsonHttpMessageConverter jacksonJsonHttpMessageConverter() {
        return new JacksonJsonHttpMessageConverter(this.objectMapper);
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (int i = 0; i < converters.size(); i++) {
            if (converters.get(i) instanceof ByteArrayHttpMessageConverter bac) {
                bac.setSupportedMediaTypes(List.of(
                        MediaType.APPLICATION_JSON,
                        new MediaType("application", "*+json"),
                        MediaType.APPLICATION_OCTET_STREAM));
                if (i != 0) {
                    converters.remove(i);
                    converters.add(0, bac);
                }
                return;
            }
        }
        ByteArrayHttpMessageConverter bac = new ByteArrayHttpMessageConverter();
        bac.setSupportedMediaTypes(List.of(
                MediaType.APPLICATION_JSON,
                new MediaType("application", "*+json"),
                MediaType.APPLICATION_OCTET_STREAM));
        converters.add(0, bac);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new PageableHandlerMethodArgumentResolver());
    }

    @Bean
    public PasswordEncoder passwordEncoder() throws NoSuchAlgorithmException {
        return new BCryptPasswordEncoder(10, SecureRandom.getInstanceStrong());
    }

    @Bean
    public RedisClient redisClient() {
        if (redisURL == null || redisURL.isBlank()) return null;

        return RedisClient.create(redisURL);
    }

    @Bean
    public RedisAsyncCommands<String, Object> asyncCommands(@Autowired(required = false) RedisClient client) {
        if (client == null) return null;

        StatefulRedisConnection<String, Object> connection = client.connect(objectCodec);
        return connection.async();
    }

    @Bean
    public StatefulRedisPubSubConnection<String, String> subConnection(
            @Autowired(required = false) RedisClient client) {
        if (client == null) return null;

        return client.connectPubSub();
    }

    @Bean
    public RedisPubSubAsyncCommands<String, String> subRedisAsyncCommand(
            @Autowired(required = false) StatefulRedisPubSubConnection<String, String> connection) {
        if (connection == null) return null;

        return connection.async();
    }

    @Bean
    public RedisPubSubAsyncCommands<String, String> pubRedisAsyncCommand(
            @Autowired(required = false) RedisClient client) {
        if (client == null) return null;

        return client.connectPubSub().async();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:3000", "http://localhost:8080")
                .allowedMethods("*")
                .maxAge(3600);
    }

    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder().expireAfterAccess(Duration.ofMinutes(5));
    }

    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        caffeineCacheManager.setCaffeine(caffeine);
        return caffeineCacheManager;
    }
}
