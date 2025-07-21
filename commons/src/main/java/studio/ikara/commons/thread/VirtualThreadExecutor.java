package studio.ikara.commons.thread;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class VirtualThreadExecutor {

    private static final ExecutorService vtExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public static <T> CompletableFuture<T> async(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, vtExecutor);
    }

    public static <T> CompletableFuture<T> just(T value) {
        return CompletableFuture.completedFuture(value);
    }

    public static <T> CompletableFuture<List<T>> all(List<Supplier<T>> tasks) {
        List<CompletableFuture<T>> futures = tasks.stream()
                .map(supplier -> CompletableFuture.supplyAsync(supplier, vtExecutor))
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream().map(CompletableFuture::join).toList());
    }

    public static <T> CompletableFuture<T> error(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, vtExecutor).exceptionally(throwable -> {
            log.error("Error executing task in virtual thread", throwable);
            throw new CompletionException(throwable);
        });
    }

    public static CompletableFuture<Void> delay(long delay) {
        return CompletableFuture.runAsync(
                () -> {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new CompletionException(e);
                    }
                },
                vtExecutor);
    }

    public static void shutdown() {
        vtExecutor.shutdown();
        try {
            if (!vtExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                vtExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            vtExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
