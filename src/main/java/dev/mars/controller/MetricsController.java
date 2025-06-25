package dev.mars.controller;

import com.google.inject.Inject;
import dev.mars.service.cache.CacheService;
import dev.mars.service.metrics.MetricsService;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Controller for metrics and health endpoints.
 */
public class MetricsController {
    private static final Logger logger = LoggerFactory.getLogger(MetricsController.class);
    
    private final MetricsService metricsService;
    private final CacheService cacheService;

    @Inject
    public MetricsController(MetricsService metricsService, CacheService cacheService) {
        this.metricsService = metricsService;
        this.cacheService = cacheService;
    }

    /**
     * Returns Prometheus metrics.
     */
    public void getMetrics(Context ctx) {
        try {
            String metrics = metricsService.getPrometheusMetrics();
            ctx.contentType("text/plain; version=0.0.4; charset=utf-8")
               .result(metrics);
        } catch (Exception e) {
            logger.error("Error retrieving metrics", e);
            ctx.status(500).json(Map.of("error", "Failed to retrieve metrics"));
        }
    }

    /**
     * Returns application health status.
     */
    public void getHealth(Context ctx) {
        try {
            Map<String, Object> health = Map.of(
                "status", "UP",
                "timestamp", System.currentTimeMillis(),
                "cache", getCacheHealth(),
                "database", getDatabaseHealth()
            );
            ctx.json(health);
        } catch (Exception e) {
            logger.error("Error checking health", e);
            ctx.status(503).json(Map.of(
                "status", "DOWN",
                "timestamp", System.currentTimeMillis(),
                "error", "Health check failed"
            ));
        }
    }

    /**
     * Returns cache statistics.
     */
    public void getCacheStats(Context ctx) {
        try {
            CacheService.CacheStats stats = cacheService.getStats();
            ctx.json(Map.of(
                "hitCount", stats.getHitCount(),
                "missCount", stats.getMissCount(),
                "evictionCount", stats.getEvictionCount(),
                "size", stats.getSize(),
                "hitRate", stats.getHitRate()
            ));
        } catch (Exception e) {
            logger.error("Error retrieving cache stats", e);
            ctx.status(500).json(Map.of("error", "Failed to retrieve cache statistics"));
        }
    }

    private Map<String, Object> getCacheHealth() {
        try {
            CacheService.CacheStats stats = cacheService.getStats();
            return Map.of(
                "status", "UP",
                "hitRate", stats.getHitRate(),
                "size", stats.getSize()
            );
        } catch (Exception e) {
            return Map.of("status", "DOWN", "error", e.getMessage());
        }
    }

    private Map<String, Object> getDatabaseHealth() {
        // Simple database health check - in a real application, you'd ping the database
        try {
            return Map.of("status", "UP", "type", "H2");
        } catch (Exception e) {
            return Map.of("status", "DOWN", "error", e.getMessage());
        }
    }
}
