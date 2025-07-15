package studio.ikara.commons.service;

import io.j13n.commons.thread.VirtualThreadExecutor;
import io.j13n.commons.thread.VirtualThreadWrapper;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.CacheType;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class CacheService extends RedisPubSubAdapter<String, String> {

    @Autowired
    private CacheManager cacheManager;

    @Autowired(required = false)
    private RedisAsyncCommands<String, Object> redisAsyncCommand;

    @Autowired(required = false)
    @Qualifier("subRedisAsyncCommand") private RedisPubSubAsyncCommands<String, String> subAsyncCommand;

    @Autowired(required = false)
    @Qualifier("pubRedisAsyncCommand") private RedisPubSubAsyncCommands<String, String> pubAsyncCommand;

    @Autowired(required = false)
    private StatefulRedisPubSubConnection<String, String> subConnect;

    @Value("${redis.channel:evictionChannel}")
    private String channel;

    @Value("${redis.cache.prefix:unk}")
    private String redisPrefix;

    @Value("${spring.cache.type:}")
    private CacheType cacheType;

    @PostConstruct
    public void registerEviction() {
        if (redisAsyncCommand == null || this.cacheType == CacheType.NONE) return;

        subAsyncCommand.subscribe(channel);
        subConnect.addListener(this);
    }

    public CompletableFuture<Boolean> evict(String cName, String key) {
        if (this.cacheType == CacheType.NONE) return VirtualThreadWrapper.just(true);

        String cacheName = this.redisPrefix + "-" + cName;

        if (pubAsyncCommand != null) {
            CompletableFuture<Boolean> publishFuture = CompletableFuture.completedFuture(
                            pubAsyncCommand.publish(this.channel, cacheName + ":" + key))
                    .thenApply(e -> true);
            CompletableFuture<Boolean> deleteFuture = CompletableFuture.completedFuture(
                            redisAsyncCommand.hdel(cacheName, key))
                    .thenApply(e -> true);
            return publishFuture.thenCompose(evicted -> deleteFuture);
        }

        return VirtualThreadWrapper.fromCallable(() -> this.caffineCacheEvict(cacheName, key))
                .exceptionally(t -> false);
    }

    private Boolean caffineCacheEvict(String cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) cache.evictIfPresent(key);
        return true;
    }

    public CompletableFuture<Boolean> evict(String cacheName, Object... keys) {
        if (this.cacheType == CacheType.NONE) return VirtualThreadWrapper.just(true);

        return makeKey(keys).thenCompose(e -> this.evict(cacheName, e));
    }

    public CompletableFuture<String> makeKey(Object... args) {
        if (args.length == 1) return VirtualThreadWrapper.just(args[0].toString());

        return VirtualThreadWrapper.fromCallable(() -> Arrays.stream(args)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.joining()));
    }

    public <T> CompletableFuture<T> put(String cName, T value, Object... keys) {
        if (this.cacheType == CacheType.NONE) return VirtualThreadWrapper.just(value);

        String cacheName = this.redisPrefix + "-" + cName;

        makeKey(keys).thenCompose(key -> {
            CacheObject co = new CacheObject(value);

            Cache cache = this.cacheManager.getCache(cacheName);
            if (cache != null) cache.put(key, co);

            if (redisAsyncCommand == null) return VirtualThreadWrapper.just(true);

            return CompletableFuture.completedFuture(redisAsyncCommand.hset(cacheName, key, co))
                    .thenApply(cObject -> true)
                    .exceptionally(ex -> true);
        });

        return VirtualThreadWrapper.just(value);
    }

    @SuppressWarnings("unchecked")
    public <T> CompletableFuture<T> get(String cName, Object... keys) {
        if (this.cacheType == CacheType.NONE) return CompletableFuture.completedFuture(null);

        String cacheName = this.redisPrefix + "-" + cName;

        return this.makeKey(keys)
                .thenCompose(key -> {
                    Cache cache = this.cacheManager.getCache(cacheName);
                    if (cache == null) return CompletableFuture.completedFuture(null);

                    CacheObject value = cache.get(key, CacheObject.class);
                    if (value != null) return CompletableFuture.completedFuture(value);

                    if (redisAsyncCommand == null) return CompletableFuture.completedFuture(null);

                    return CompletableFuture.completedFuture(redisAsyncCommand.hget(cacheName, key))
                            .thenApply(obj -> {
                                if (obj != null) {
                                    CacheObject redisValue = (CacheObject) obj;
                                    cache.put(key, redisValue);
                                    return redisValue;
                                }
                                return null;
                            })
                            .exceptionally(ex -> null);
                })
                .thenApply(e -> e != null ? (T) e.getObject() : null);
    }

    @SuppressWarnings("unchecked")
    public <T> CompletableFuture<T> cacheValueOrGet(
            String cName, Supplier<CompletableFuture<T>> supplier, Object... keys) {
        return this.makeKey(keys)
                .thenCompose(key -> this.get(cName, key).thenCompose(result -> {
                    if (result != null) return CompletableFuture.completedFuture(result);

                    return supplier.get().thenCompose(value -> {
                        if (value == null) return CompletableFuture.completedFuture(null);

                        return this.put(cName, value, key);
                    });
                }))
                .thenApply(e -> (T) e);
    }

    @SuppressWarnings("unchecked")
    public <T> CompletableFuture<T> cacheEmptyValueOrGet(
            String cName, Supplier<CompletableFuture<T>> supplier, Object... keys) {
        return this.makeKey(keys)
                .thenCompose(key -> this.<CacheObject>get(cName, key).thenCompose(result -> {
                    if (result != null) return CompletableFuture.completedFuture(result);

                    return supplier.get()
                            .thenCompose(value -> {
                                CacheObject cacheObj = new CacheObject(value);
                                return this.put(cName, cacheObj, key).thenApply(cObject -> cacheObj);
                            })
                            .exceptionally(ex -> {
                                CacheObject nullObj = new CacheObject(null);
                                this.put(cName, nullObj, key);
                                return nullObj;
                            });
                }))
                .thenApply(e -> e != null ? (T) e.getObject() : null);
    }

    public CompletableFuture<Boolean> evictAll(String cName) {
        if (this.cacheType == CacheType.NONE) return VirtualThreadWrapper.just(true);

        String cacheName = this.redisPrefix + "-" + cName;

        if (pubAsyncCommand != null) {
            CompletableFuture<Boolean> publishFuture = CompletableFuture.completedFuture(
                            pubAsyncCommand.publish(this.channel, cacheName + ":*"))
                    .thenApply(e -> true);
            CompletableFuture<Boolean> deleteFuture = CompletableFuture.completedFuture(
                            redisAsyncCommand.del(cacheName))
                    .thenApply(e -> true)
                    .exceptionally(ex -> true);
            return publishFuture.thenCompose(evicted -> deleteFuture);
        }

        return VirtualThreadWrapper.fromCallable(() -> {
                    this.cacheManager.getCache(cacheName).clear();
                    return true;
                })
                .exceptionally(evicted -> false);
    }

    public CompletableFuture<Boolean> evictAllCaches() {
        if (this.cacheType == CacheType.NONE) return VirtualThreadWrapper.just(true);

        if (pubAsyncCommand != null) {
            return CompletableFuture.completedFuture(redisAsyncCommand.keys(this.redisPrefix + "-*"))
                    .thenCompose(keysFuture -> VirtualThreadWrapper.fromCallable(() -> {
                                try {
                                    List<String> keys = keysFuture.get();
                                    List<CompletableFuture<Boolean>> futures = new ArrayList<>();

                                    for (String key : keys) {
                                        CompletableFuture<Boolean> publishFuture = CompletableFuture.completedFuture(
                                                        pubAsyncCommand.publish(this.channel, key + ":*"))
                                                .thenApply(e -> true);
                                        CompletableFuture<Boolean> deleteFuture = CompletableFuture.completedFuture(
                                                        redisAsyncCommand.del(key))
                                                .thenApply(e -> true);
                                        futures.add(publishFuture.thenCompose(evicted -> deleteFuture));
                                    }

                                    return futures;
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            })
                            .thenCompose(futures -> {
                                if (futures.isEmpty()) {
                                    return CompletableFuture.completedFuture(List.of(true));
                                }
                                return VirtualThreadExecutor.all(futures.stream()
                                        .map(f -> (Supplier<Boolean>) f::join)
                                        .toList());
                            }))
                    .thenApply(results -> results.stream().allMatch(Boolean::booleanValue));
        }

        return VirtualThreadWrapper.fromCallable(() -> {
            Collection<String> cacheNames = this.cacheManager.getCacheNames();
            boolean result = true;

            for (String name : cacheNames) {
                Cache cache = this.cacheManager.getCache(name);
                if (cache != null) {
                    cache.clear();
                } else {
                    result = false;
                }
            }

            return result;
        });
    }

    public CompletableFuture<Collection<String>> getCacheNames() {
        return VirtualThreadWrapper.just(this.cacheManager.getCacheNames().stream()
                .map(e -> e.substring(this.redisPrefix.length() + 1))
                .toList());
    }

    @Override
    public void message(String channel, String message) {
        if (channel == null || !channel.equals(this.channel)) return;

        int colon = message.indexOf(':');
        if (colon == -1) return;

        String cacheName = message.substring(0, colon);
        String cacheKey = message.substring(colon + 1);
        Cache cache = this.cacheManager.getCache(cacheName);
        if (cache == null) return;

        if (cacheKey.equals("*")) cache.clear();
        else cache.evictIfPresent(cacheKey);
    }

    public <T> Function<T, CompletableFuture<T>> evictAllFunction(String cacheName) {
        return v -> this.evictAll(cacheName).thenApply(e -> v);
    }

    public <T> Function<T, CompletableFuture<T>> evictFunction(String cacheName, Object... keys) {
        return v -> this.evict(cacheName, keys).thenApply(e -> v);
    }

    @SuppressWarnings("unchecked")
    public <T> Function<T, CompletableFuture<T>> evictFunctionWithSuppliers(
            String cacheName, Supplier<Object>... keySuppliers) {
        Object[] keys = new Object[keySuppliers.length];

        for (int i = 0; i < keySuppliers.length; i++) keys[i] = keySuppliers[i].get();

        return v -> this.evict(cacheName, keys).thenApply(evicted -> v);
    }

    public <T> Function<T, CompletableFuture<T>> evictFunctionWithKeyFunction(
            String cacheName, Function<T, String> keyMakingFunction) {
        return v -> this.evict(cacheName, keyMakingFunction.apply(v)).thenApply(evicted -> v);
    }

    private Boolean get() {
        Collection<String> cacheNames = this.cacheManager.getCacheNames();
        boolean result = true;

        for (String name : cacheNames) {
            Cache cache = this.cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            } else {
                result = false;
            }
        }

        return result;
    }
}
