package dev.mars.service.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.mars.config.ApplicationProperties;
import dev.mars.service.metrics.MetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Caffeine-based cache implementation.
 */
@Singleton
public class CaffeineCache implements CacheService {
    private static final Logger logger = LoggerFactory.getLogger(CaffeineCache.class);
    
    private final Cache<String, Object> cache;
    private final MetricsService metricsService;
    private final boolean cacheEnabled;

    @Inject
    public CaffeineCache(ApplicationProperties properties, MetricsService metricsService) {
        this.metricsService = metricsService;
        this.cacheEnabled = properties.getCache().isEnabled();
        
        if (cacheEnabled) {
            ApplicationProperties.CacheConfig config = properties.getCache();
            this.cache = Caffeine.newBuilder()
                .maximumSize(config.getMaxSize())
                .expireAfterWrite(Duration.ofMinutes(config.getExpireAfterWriteMinutes()))
                .recordStats()
                .build();
            
            logger.info("Caffeine cache initialized with maxSize={}, expireAfterWrite={}min", 
                config.getMaxSize(), config.getExpireAfterWriteMinutes());
        } else {
            this.cache = null;
            logger.info("Cache disabled");
        }
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        if (!cacheEnabled) {
            return Optional.empty();
        }
        
        try {
            Object value = cache.getIfPresent(key);
            if (value != null) {
                metricsService.incrementCounter("cache.hits");
                logger.trace("Cache hit for key: {}", key);
                return Optional.of(type.cast(value));
            } else {
                metricsService.incrementCounter("cache.misses");
                logger.trace("Cache miss for key: {}", key);
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.warn("Cache get operation failed for key: {}", key, e);
            metricsService.incrementCounter("cache.errors");
            return Optional.empty();
        }
    }

    @Override
    public void put(String key, Object value) {
        if (!cacheEnabled || value == null) {
            return;
        }
        
        try {
            cache.put(key, value);
            logger.trace("Cached value for key: {}", key);
        } catch (Exception e) {
            logger.warn("Cache put operation failed for key: {}", key, e);
            metricsService.incrementCounter("cache.errors");
        }
    }

    @Override
    public <T> T getOrCompute(String key, Class<T> type, Supplier<T> supplier) {
        if (!cacheEnabled) {
            return supplier.get();
        }
        
        return metricsService.timeOperation("cache.operation.duration", () -> {
            Optional<T> cached = get(key, type);
            if (cached.isPresent()) {
                return cached.get();
            }
            
            T computed = supplier.get();
            if (computed != null) {
                put(key, computed);
            }
            return computed;
        });
    }

    @Override
    public <T> CompletableFuture<T> getOrComputeAsync(String key, Class<T> type, Supplier<CompletableFuture<T>> supplier) {
        if (!cacheEnabled) {
            return supplier.get();
        }
        
        Optional<T> cached = get(key, type);
        if (cached.isPresent()) {
            return CompletableFuture.completedFuture(cached.get());
        }
        
        return supplier.get().thenApply(computed -> {
            if (computed != null) {
                put(key, computed);
            }
            return computed;
        });
    }

    @Override
    public void evict(String key) {
        if (!cacheEnabled) {
            return;
        }
        
        try {
            cache.invalidate(key);
            logger.trace("Evicted cache entry for key: {}", key);
        } catch (Exception e) {
            logger.warn("Cache eviction failed for key: {}", key, e);
            metricsService.incrementCounter("cache.errors");
        }
    }

    @Override
    public void evictAll() {
        if (!cacheEnabled) {
            return;
        }
        
        try {
            cache.invalidateAll();
            logger.info("Evicted all cache entries");
        } catch (Exception e) {
            logger.warn("Cache clear operation failed", e);
            metricsService.incrementCounter("cache.errors");
        }
    }

    @Override
    public CacheService.CacheStats getStats() {
        if (!cacheEnabled) {
            return new CacheService.CacheStats(0, 0, 0, 0);
        }

        try {
            com.github.benmanes.caffeine.cache.stats.CacheStats caffeineStats = cache.stats();
            return new CacheService.CacheStats(
                caffeineStats.hitCount(),
                caffeineStats.missCount(),
                caffeineStats.evictionCount(),
                cache.estimatedSize()
            );
        } catch (Exception e) {
            logger.warn("Failed to get cache stats", e);
            return new CacheService.CacheStats(0, 0, 0, 0);
        }
    }

    /**
     * Force cache cleanup to trigger evictions (for testing purposes).
     */
    public void forceCleanup() {
        if (cacheEnabled) {
            cache.cleanUp();
        }
    }
}
