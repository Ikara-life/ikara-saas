package studio.ikara.commons.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.j13n.commons.codec.RedisJSONCodec;
import io.j13n.commons.codec.RedisObjectCodec;
import io.j13n.commons.gson.LocalDateTimeAdapter;
import io.j13n.commons.jackson.CommonsSerializationModule;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public abstract class AbstractBaseConfiguration implements WebMvcConfigurer {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractBaseConfiguration.class);

    protected ObjectMapper objectMapper;

    @Value("${redis.url:}")
    private String redisURL;

    @Value("${redis.codec:object}")
    private String codecType;

    private RedisCodec<String, Object> objectCodec;

    protected AbstractBaseConfiguration(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    protected void initialize() {
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.setDefaultPropertyInclusion(JsonInclude.Value.construct(Include.NON_NULL, Include.ALWAYS));
        this.objectMapper.setDefaultPropertyInclusion(JsonInclude.Value.construct(Include.NON_EMPTY, Include.ALWAYS));
        this.objectMapper.registerModule(new CommonsSerializationModule());
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
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(this.objectMapper);
        return converter;
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
