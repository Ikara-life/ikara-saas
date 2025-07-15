package studio.ikara.commons.thread;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Utility class for managing virtual threads with similar functionality to reactive programming.
 * This class provides methods to execute tasks in virtual threads and handle their results.
 */
@Slf4j
public class VirtualThreadExecutor {
    private static final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * Executes a task in a virtual thread and returns a CompletableFuture with the result.
     *
     * @param supplier The task to execute
     * @param <T> The type of the result
     * @return A CompletableFuture containing the result of the task
     */
    public static <T> CompletableFuture<T> async(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, virtualThreadExecutor);
    }

    /**
     * Executes a task in a virtual thread and returns a CompletableFuture with the result.
     * This method is similar to Mono.just() in reactive programming.
     *
     * @param value The value to wrap in a CompletableFuture
     * @param <T> The type of the value
     * @return A CompletableFuture containing the value
     */
    public static <T> CompletableFuture<T> just(T value) {
        return CompletableFuture.completedFuture(value);
    }

    /**
     * Executes multiple tasks in parallel using virtual threads and returns a CompletableFuture
     * with a list of results. This is similar to Flux.fromIterable() in reactive programming.
     *
     * @param tasks The list of tasks to execute
     * @param <T> The type of the results
     * @return A CompletableFuture containing a list of results
     */
    public static <T> CompletableFuture<List<T>> all(List<Supplier<T>> tasks) {
        List<CompletableFuture<T>> futures = tasks.stream()
                .map(supplier -> CompletableFuture.supplyAsync(supplier, virtualThreadExecutor))
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream().map(CompletableFuture::join).toList());
    }

    /**
     * Executes a task in a virtual thread and handles any exceptions that occur.
     * This is similar to Mono.error() in reactive programming.
     *
     * @param supplier The task to execute
     * @param <T> The type of the result
     * @return A CompletableFuture containing the result or an exception
     */
    public static <T> CompletableFuture<T> error(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, virtualThreadExecutor).exceptionally(throwable -> {
            log.error("Error executing task in virtual thread", throwable);
            throw new CompletionException(throwable);
        });
    }

    /**
     * Executes a task in a virtual thread and returns a CompletableFuture that completes
     * after the specified delay. This is similar to Mono.delay() in reactive programming.
     *
     * @param delay The delay in milliseconds
     * @return A CompletableFuture that completes after the delay
     */
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
                virtualThreadExecutor);
    }

    /**
     * Shuts down the virtual thread executor.
     * This should be called when the application is shutting down.
     */
    public static void shutdown() {
        virtualThreadExecutor.shutdown();
        try {
            if (!virtualThreadExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                virtualThreadExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            virtualThreadExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
