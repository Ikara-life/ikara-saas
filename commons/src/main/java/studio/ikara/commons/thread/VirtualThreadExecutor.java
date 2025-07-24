package studio.ikara.commons.thread;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class VirtualThreadExecutor {

    private static final AtomicReference<ExecutorService> EXECUTOR =
            new AtomicReference<>(createOptimizedVirtualExecutor());

    static {
        log.info("VirtualThreadExecutor initialized with optimized configuration");
        Runtime.getRuntime().addShutdownHook(Thread.ofVirtual()
                .name("VirtualThreadExecutor-Shutdown")
                .start(() -> {
                    log.info("VirtualThreadExecutor shutting down gracefully");
                    shutdown();
                }));
    }

    // ================== CONFIGURATION METHODS ==================

    /**
     * Creates an optimized virtual thread executor with better resource management
     */
    private static ExecutorService createOptimizedVirtualExecutor() {
        return Executors.newThreadPerTaskExecutor(Thread.ofVirtual()
                .name("VirtualTask-", 0)
                .factory());
    }

    /**
     * Configure a custom ExecutorService
     */
    public static void configure(ExecutorService customExecutor) {
        if (customExecutor != null) {
            ExecutorService old = EXECUTOR.getAndSet(customExecutor);
            shutdownExecutor(old);
            log.info("Custom ExecutorService configured for VirtualThreadExecutor");
        }
    }

    /**
     * Get current executor (for advanced usage)
     */
    public static ExecutorService getCurrentExecutor() {
        return EXECUTOR.get();
    }

    // ================== BASIC ASYNC OPERATIONS ==================

    /**
     * Execute a supplier asynchronously with improved error handling
     */
    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, EXECUTOR.get())
                .whenComplete((res, err) -> {
                    if (err != null) {
                        log.error("Async task failed: {}", err.getMessage(), err);
                    }
                });
    }

    /**
     * Create a completed future
     */
    public static <T> CompletableFuture<T> completedFuture(T value) {
        return CompletableFuture.completedFuture(value);
    }

    /**
     * Create a failed future
     */
    public static <T> CompletableFuture<T> failedFuture(Throwable throwable) {
        return CompletableFuture.failedFuture(throwable);
    }

    /**
     * Execute task with explicit error logging and re-throwing
     */
    public static <T> CompletableFuture<T> withErrorLogging(Supplier<T> task) {
        return CompletableFuture.supplyAsync(task, EXECUTOR.get())
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.error("Task execution failed", throwable);
                    }
                });
    }

    // ================== PARALLEL EXECUTION ==================

    /**
     * Execute all tasks in parallel without concurrency limit
     */
    public static <T> CompletableFuture<List<T>> all(List<Supplier<T>> tasks) {
        return all(tasks, Integer.MAX_VALUE);
    }

    /**
     * Execute all tasks in parallel with concurrency limit using optimized approach
     */
    public static <T> CompletableFuture<List<T>> all(List<Supplier<T>> tasks, int concurrencyLimit) {
        if (tasks.isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        }

        if (concurrencyLimit <= 0) {
            throw new IllegalArgumentException("Concurrency limit must be positive");
        }

        Semaphore semaphore = new Semaphore(concurrencyLimit);
        List<CompletableFuture<T>> futures = tasks.stream()
                .map(task -> CompletableFuture.supplyAsync(() -> {
                    try {
                        semaphore.acquire();
                        return task.get();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new CompletionException(e);
                    } finally {
                        semaphore.release();
                    }
                }, EXECUTOR.get()))
                .toList();

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .toList());
    }

    /**
     * Race multiple tasks - return the first successful result
     */
    public static <T> CompletableFuture<T> race(List<Supplier<T>> tasks) {
        if (tasks.isEmpty()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("No tasks provided"));
        }

        List<CompletableFuture<T>> futures = tasks.stream()
                .map(VirtualThreadExecutor::supplyAsync)
                .toList();

        return CompletableFuture.anyOf(futures.toArray(CompletableFuture[]::new))
                .thenApply(result -> (T) result);
    }

    /**
     * Execute tasks and return results as they complete (streaming results)
     */
    public static <T> CompletableFuture<List<T>> allSettled(List<Supplier<T>> tasks) {
        if (tasks.isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        }

        List<CompletableFuture<T>> futures = tasks.stream()
                .map(task -> supplyAsync(task).exceptionally(throwable -> {
                    log.warn("Task failed in allSettled: {}", throwable.getMessage());
                    return null; // Return null for failed tasks
                }))
                .toList();

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Objects::nonNull) // Filter out failed tasks
                        .toList());
    }

    // ================== TIMING AND TIMEOUT ==================

    /**
     * Add timeout to a CompletableFuture using Duration
     */
    public static <T> CompletableFuture<T> withTimeout(CompletableFuture<T> future, Duration timeout) {
        return future.orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Add timeout with custom TimeUnit
     */
    public static <T> CompletableFuture<T> withTimeout(CompletableFuture<T> future, long timeout, TimeUnit unit) {
        return future.orTimeout(timeout, unit);
    }

    /**
     * Create a delay using Duration
     */
    public static CompletableFuture<Void> delay(Duration duration) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(duration);
                return null;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new CompletionException(e);
            }
        }, EXECUTOR.get());
    }

    /**
     * Create a delay using milliseconds (legacy compatibility)
     */
    public static CompletableFuture<Void> delay(long delayMillis) {
        return delay(Duration.ofMillis(delayMillis));
    }

    // ================== TRANSFORMATION METHODS ==================

    /**
     * Compose async operations
     */
    public static <T, R> CompletableFuture<R> thenComposeAsync(
            CompletableFuture<T> future, Function<T, CompletableFuture<R>> mapper) {
        return future.thenComposeAsync(mapper, EXECUTOR.get());
    }

    /**
     * Transform results async
     */
    public static <T, R> CompletableFuture<R> thenApplyAsync(
            CompletableFuture<T> future, Function<T, R> mapper) {
        return future.thenApplyAsync(mapper, EXECUTOR.get());
    }

    /**
     * Chain multiple async operations
     */
    public static <T> CompletableFuture<T> chain(T initialValue, List<Function<T, CompletableFuture<T>>> operations) {
        CompletableFuture<T> result = CompletableFuture.completedFuture(initialValue);

        for (Function<T, CompletableFuture<T>> operation : operations) {
            result = result.thenComposeAsync(operation, EXECUTOR.get());
        }

        return result;
    }

    // ================== ERROR HANDLING ==================

    /**
     * Handle errors with recovery function
     */
    public static <T> CompletableFuture<T> onErrorResume(
            CompletableFuture<T> future, Function<Throwable, CompletableFuture<T>> handler) {
        return future.handle((res, ex) ->
                        ex != null ? handler.apply(ex) : CompletableFuture.completedFuture(res))
                .thenCompose(Function.identity());
    }

    /**
     * Provide default value on exception
     */
    public static <T> CompletableFuture<T> exceptionally(CompletableFuture<T> future, T defaultValue) {
        return future.exceptionally(throwable -> {
            log.warn("Task failed, using default value: {}", throwable.getMessage());
            return defaultValue;
        });
    }

    /**
     * Provide default value using supplier on exception
     */
    public static <T> CompletableFuture<T> exceptionally(CompletableFuture<T> future, Supplier<T> defaultSupplier) {
        return future.exceptionally(throwable -> {
            log.warn("Task failed, computing default value: {}", throwable.getMessage());
            return defaultSupplier.get();
        });
    }

    /**
     * Execute with fallback chain
     */
    public static <T> CompletableFuture<T> withFallback(List<Supplier<T>> suppliers) {
        if (suppliers.isEmpty()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("No suppliers provided"));
        }

        CompletableFuture<T> result = supplyAsync(suppliers.get(0));

        for (int i = 1; i < suppliers.size(); i++) {
            final Supplier<T> fallback = suppliers.get(i);
            result = result.exceptionallyCompose(throwable -> {
                log.warn("Primary supplier failed, trying fallback: {}", throwable.getMessage());
                return supplyAsync(fallback);
            });
        }

        return result;
    }

    // ================== RETRY MECHANISMS ==================

    /**
     * Retry a task with exponential backoff
     */
    public static <T> CompletableFuture<T> retry(Supplier<T> task, int maxAttempts, Duration initialDelay) {
        return retryInternal(task, maxAttempts, initialDelay, 1);
    }

    /**
     * Retry a task with fixed delay
     */
    public static <T> CompletableFuture<T> retryFixed(Supplier<T> task, int maxAttempts, Duration delay) {
        return retryFixedInternal(task, maxAttempts, delay, 1);
    }

    private static <T> CompletableFuture<T> retryInternal(Supplier<T> task, int attemptsLeft, Duration delay, int attempt) {
        return supplyAsync(task)
                .exceptionallyCompose(throwable -> {
                    if (attemptsLeft <= 1) {
                        log.error("Task failed after {} attempts", attempt);
                        return CompletableFuture.failedFuture(throwable);
                    }

                    log.warn("Task failed on attempt {}, retrying in {}", attempt, delay);
                    return delay(delay)
                            .thenCompose(v -> retryInternal(task, attemptsLeft - 1,
                                    Duration.ofMillis((long) (delay.toMillis() * 1.5)), attempt + 1));
                });
    }

    private static <T> CompletableFuture<T> retryFixedInternal(Supplier<T> task, int attemptsLeft, Duration delay, int attempt) {
        return supplyAsync(task)
                .exceptionallyCompose(throwable -> {
                    if (attemptsLeft <= 1) {
                        log.error("Task failed after {} attempts", attempt);
                        return CompletableFuture.failedFuture(throwable);
                    }

                    log.warn("Task failed on attempt {}, retrying in {}", attempt, delay);
                    return delay(delay)
                            .thenCompose(v -> retryFixedInternal(task, attemptsLeft - 1, delay, attempt + 1));
                });
    }

    // ================== BATCH PROCESSING ==================

    /**
     * Process items in batches with controlled concurrency
     */
    public static <T, R> CompletableFuture<List<R>> processBatches(
            List<T> items, Function<T, R> processor, int batchSize, int concurrency) {

        if (items.isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        }

        List<List<T>> batches = partition(items, batchSize);
        List<Supplier<List<R>>> batchTasks = batches.stream()
                .map(batch -> (Supplier<List<R>>) () ->
                        batch.stream().map(processor).toList())
                .toList();

        return all(batchTasks, concurrency)
                .thenApply(batchResults -> batchResults.stream()
                        .flatMap(List::stream)
                        .toList());
    }

    private static <T> List<List<T>> partition(List<T> list, int size) {
        return list.stream()
                .collect(java.util.stream.Collectors.groupingBy(item -> list.indexOf(item) / size))
                .values()
                .stream()
                .toList();
    }

    // ================== RESOURCE MANAGEMENT ==================

    /**
     * Shutdown the executor gracefully
     */
    public static void shutdown() {
        ExecutorService current = EXECUTOR.getAndSet(createOptimizedVirtualExecutor());
        shutdownExecutor(current);
    }

    private static void shutdownExecutor(ExecutorService executor) {
        if (executor == null || executor.isShutdown()) {
            return;
        }

        log.info("Shutting down VirtualThreadExecutor...");
        try {
            executor.shutdown();
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                log.warn("Executor did not terminate gracefully, forcing shutdown...");
                executor.shutdownNow();
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    log.error("Executor did not terminate after forced shutdown");
                }
            } else {
                log.info("VirtualThreadExecutor shutdown completed successfully");
            }
        } catch (InterruptedException e) {
            log.error("Shutdown interrupted", e);
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
