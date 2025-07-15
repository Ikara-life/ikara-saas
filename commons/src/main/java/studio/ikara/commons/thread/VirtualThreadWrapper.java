package studio.ikara.commons.thread;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

public class VirtualThreadWrapper {

    public static <T> CompletableFuture<T> just(T value) {
        return VirtualThreadExecutor.just(value);
    }

    public static <T> CompletableFuture<T> fromCallable(Supplier<T> supplier) {
        return VirtualThreadExecutor.async(supplier);
    }

    public static <T, R> CompletableFuture<R> flatMap(
            CompletableFuture<T> future, Function<T, CompletableFuture<R>> mapper) {
        return future.thenCompose(mapper);
    }

    public static <T, R> CompletableFuture<R> map(CompletableFuture<T> future, Function<T, R> mapper) {
        return future.thenApply(mapper);
    }

    public static <T> CompletableFuture<List<T>> fromIterable(List<Supplier<T>> tasks) {
        return VirtualThreadExecutor.all(tasks);
    }

    public static <T> CompletableFuture<T> onErrorResume(
            CompletableFuture<T> future, Function<Throwable, CompletableFuture<T>> errorHandler) {
        return future.handle((result, throwable) -> {
                    if (throwable != null) {
                        return errorHandler.apply(throwable);
                    }
                    return CompletableFuture.completedFuture(result);
                })
                .thenCompose(Function.identity());
    }

    public static <T> CompletableFuture<T> onErrorReturn(CompletableFuture<T> future, T defaultValue) {
        return future.exceptionally(throwable -> defaultValue);
    }

    public static CompletableFuture<Void> delay(long delay) {
        return VirtualThreadExecutor.delay(delay);
    }
}
