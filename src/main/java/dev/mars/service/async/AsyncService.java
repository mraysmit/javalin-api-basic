package dev.mars.service.async;

import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Service for handling asynchronous operations.
 */
@Singleton
public class AsyncService {
    private static final Logger logger = LoggerFactory.getLogger(AsyncService.class);
    
    private final ExecutorService executorService;

    public AsyncService() {
        this.executorService = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors() * 2
        );
        logger.info("Async service initialized with {} threads", 
            Runtime.getRuntime().availableProcessors() * 2);
    }

    /**
     * Executes a task asynchronously.
     * 
     * @param task The task to execute
     * @return CompletableFuture with the result
     */
    public <T> CompletableFuture<T> executeAsync(Supplier<T> task) {
        return CompletableFuture.supplyAsync(task, executorService)
            .exceptionally(throwable -> {
                logger.error("Async task failed", throwable);
                throw new RuntimeException("Async operation failed", throwable);
            });
    }

    /**
     * Executes a task asynchronously without return value.
     * 
     * @param task The task to execute
     * @return CompletableFuture<Void>
     */
    public CompletableFuture<Void> executeAsync(Runnable task) {
        return CompletableFuture.runAsync(task, executorService)
            .exceptionally(throwable -> {
                logger.error("Async task failed", throwable);
                throw new RuntimeException("Async operation failed", throwable);
            });
    }

    /**
     * Shuts down the async service gracefully.
     */
    public void shutdown() {
        logger.info("Shutting down async service");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                logger.warn("Async service did not terminate gracefully, forcing shutdown");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting for async service shutdown", e);
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
