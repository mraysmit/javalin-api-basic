package dev.mars.service.cache;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Interface for cache operations.
 */
public interface CacheService {
    
    /**
     * Gets a value from the cache.
     * 
     * @param key The cache key
     * @param type The expected type of the cached value
     * @return Optional containing the cached value if present
     */
    <T> Optional<T> get(String key, Class<T> type);
    
    /**
     * Puts a value into the cache.
     * 
     * @param key The cache key
     * @param value The value to cache
     */
    void put(String key, Object value);
    
    /**
     * Gets a value from cache or computes it if not present.
     * 
     * @param key The cache key
     * @param type The expected type of the value
     * @param supplier The supplier to compute the value if not cached
     * @return The cached or computed value
     */
    <T> T getOrCompute(String key, Class<T> type, Supplier<T> supplier);
    
    /**
     * Gets a value from cache or computes it asynchronously if not present.
     * 
     * @param key The cache key
     * @param type The expected type of the value
     * @param supplier The supplier to compute the value if not cached
     * @return CompletableFuture with the cached or computed value
     */
    <T> CompletableFuture<T> getOrComputeAsync(String key, Class<T> type, Supplier<CompletableFuture<T>> supplier);
    
    /**
     * Removes a value from the cache.
     * 
     * @param key The cache key
     */
    void evict(String key);
    
    /**
     * Removes all values from the cache.
     */
    void evictAll();
    
    /**
     * Gets cache statistics.
     * 
     * @return CacheStats object with cache metrics
     */
    CacheStats getStats();
    
    /**
     * Cache statistics holder.
     */
    class CacheStats {
        private final long hitCount;
        private final long missCount;
        private final long evictionCount;
        private final long size;
        
        public CacheStats(long hitCount, long missCount, long evictionCount, long size) {
            this.hitCount = hitCount;
            this.missCount = missCount;
            this.evictionCount = evictionCount;
            this.size = size;
        }
        
        public long getHitCount() { return hitCount; }
        public long getMissCount() { return missCount; }
        public long getEvictionCount() { return evictionCount; }
        public long getSize() { return size; }
        public double getHitRate() { 
            long total = hitCount + missCount;
            return total == 0 ? 0.0 : (double) hitCount / total;
        }
        
        @Override
        public String toString() {
            return String.format("CacheStats{hits=%d, misses=%d, evictions=%d, size=%d, hitRate=%.2f%%}", 
                hitCount, missCount, evictionCount, size, getHitRate() * 100);
        }
    }
}
