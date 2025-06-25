package dev.mars.performance;

import dev.mars.config.ApplicationProperties;
import dev.mars.service.cache.CaffeineCache;
import dev.mars.service.cache.CacheService;
import dev.mars.service.metrics.MetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Performance tests for the caching layer.
 */
public class CachePerformanceTest {

    @Mock
    private MetricsService metricsService;

    private CacheService cacheService;
    private ApplicationProperties properties;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Create test configuration
        properties = new ApplicationProperties();
        properties.getCache().setEnabled(true);
        properties.getCache().setMaxSize(10000);
        properties.getCache().setExpireAfterWriteMinutes(30);
        
        cacheService = new CaffeineCache(properties, metricsService);
    }

    @Test
    void testCachePerformanceUnderLoad() throws InterruptedException {
        int numberOfOperations = 10000;
        int numberOfThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        long startTime = System.currentTimeMillis();

        // Submit cache operations
        CompletableFuture<?>[] futures = IntStream.range(0, numberOfOperations)
            .mapToObj(i -> CompletableFuture.runAsync(() -> {
                String key = "test-key-" + (i % 1000); // Create some key overlap for cache hits
                String value = "test-value-" + i;
                
                // Put operation
                cacheService.put(key, value);
                
                // Get operation
                cacheService.get(key, String.class);
                
                // GetOrCompute operation
                cacheService.getOrCompute(key + "-computed", String.class, () -> "computed-" + i);
            }, executor))
            .toArray(CompletableFuture[]::new);

        // Wait for all operations to complete
        CompletableFuture.allOf(futures).join();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        // Performance assertions
        assertTrue(duration < 5000, "Cache operations should complete within 5 seconds");
        
        CacheService.CacheStats stats = cacheService.getStats();
        assertTrue(stats.getHitCount() > 0, "Should have cache hits");
        assertTrue(stats.getSize() > 0, "Cache should contain entries");
        
        System.out.printf("Cache performance test completed in %d ms%n", duration);
        System.out.printf("Cache stats: %s%n", stats);
    }

    @Test
    void testCacheHitRateOptimization() {
        int numberOfKeys = 100;
        int numberOfAccesses = 1000;

        // Populate cache
        for (int i = 0; i < numberOfKeys; i++) {
            cacheService.put("key-" + i, "value-" + i);
        }

        // Access keys multiple times (should result in high hit rate)
        for (int i = 0; i < numberOfAccesses; i++) {
            String key = "key-" + (i % numberOfKeys);
            cacheService.get(key, String.class);
        }

        CacheService.CacheStats stats = cacheService.getStats();
        double hitRate = stats.getHitRate();
        
        assertTrue(hitRate > 0.8, "Hit rate should be above 80% for repeated access pattern");
        System.out.printf("Cache hit rate: %.2f%%%n", hitRate * 100);
    }

    @Test
    void testCacheEvictionBehavior() {
        // Set small cache size for testing eviction
        ApplicationProperties smallCacheProps = new ApplicationProperties();
        smallCacheProps.getCache().setEnabled(true);
        smallCacheProps.getCache().setMaxSize(10);
        smallCacheProps.getCache().setExpireAfterWriteMinutes(30);
        
        CacheService smallCache = new CaffeineCache(smallCacheProps, metricsService);

        // Fill cache beyond capacity
        for (int i = 0; i < 20; i++) {
            smallCache.put("key-" + i, "value-" + i);
        }

        CacheService.CacheStats stats = smallCache.getStats();
        assertTrue(stats.getSize() <= 10, "Cache size should not exceed maximum");
        assertTrue(stats.getEvictionCount() > 0, "Should have evictions when exceeding capacity");
        
        System.out.printf("Small cache stats: %s%n", stats);
    }

    @Test
    void testAsyncCacheOperations() throws Exception {
        int numberOfAsyncOps = 1000;
        
        CompletableFuture<?>[] futures = IntStream.range(0, numberOfAsyncOps)
            .mapToObj(i -> cacheService.getOrComputeAsync(
                "async-key-" + i, 
                String.class, 
                () -> CompletableFuture.supplyAsync(() -> {
                    // Simulate some async work
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return "async-value-" + i;
                })
            ))
            .toArray(CompletableFuture[]::new);

        long startTime = System.currentTimeMillis();
        CompletableFuture.allOf(futures).join();
        long duration = System.currentTimeMillis() - startTime;

        assertTrue(duration < 3000, "Async cache operations should complete efficiently");
        
        CacheService.CacheStats stats = cacheService.getStats();
        assertTrue(stats.getSize() >= numberOfAsyncOps, "All async values should be cached");
        
        System.out.printf("Async cache operations completed in %d ms%n", duration);
    }
}
