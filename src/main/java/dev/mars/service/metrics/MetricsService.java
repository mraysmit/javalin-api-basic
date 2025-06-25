package dev.mars.service.metrics;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.mars.config.ApplicationProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Service for managing application metrics and monitoring.
 */
@Singleton
public class MetricsService {
    private static final Logger logger = LoggerFactory.getLogger(MetricsService.class);
    
    private final MeterRegistry meterRegistry;
    private final ConcurrentMap<String, Counter> counters = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Timer> timers = new ConcurrentHashMap<>();
    private final boolean metricsEnabled;

    @Inject
    public MetricsService(ApplicationProperties properties) {
        this.metricsEnabled = properties.getMetrics().isEnabled();
        
        if (metricsEnabled) {
            this.meterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
            initializeDefaultMetrics();
            logger.info("Metrics service initialized with Prometheus registry");
        } else {
            this.meterRegistry = null;
            logger.info("Metrics service disabled");
        }
    }

    private void initializeDefaultMetrics() {
        // HTTP request metrics
        getCounter("http.requests.total", "Total HTTP requests");
        getCounter("http.requests.errors", "Total HTTP error responses");
        getTimer("http.request.duration", "HTTP request duration");
        
        // Business metrics
        getCounter("users.created", "Total users created");
        getCounter("users.updated", "Total users updated");
        getCounter("users.deleted", "Total users deleted");
        getCounter("trades.created", "Total trades created");
        getCounter("trades.updated", "Total trades updated");
        getCounter("trades.deleted", "Total trades deleted");
        
        // Cache metrics
        getCounter("cache.hits", "Cache hits");
        getCounter("cache.misses", "Cache misses");
        getTimer("cache.operation.duration", "Cache operation duration");
        
        logger.debug("Default metrics initialized");
    }

    /**
     * Increments a counter metric.
     * 
     * @param name The metric name
     * @param tags Optional tags as key-value pairs
     */
    public void incrementCounter(String name, String... tags) {
        if (!metricsEnabled) return;
        
        try {
            getCounter(name, "Counter metric").increment();
            logger.trace("Incremented counter: {}", name);
        } catch (Exception e) {
            logger.warn("Failed to increment counter: {}", name, e);
        }
    }

    /**
     * Records a timer metric.
     * 
     * @param name The metric name
     * @param duration The duration to record
     * @param tags Optional tags as key-value pairs
     */
    public void recordTimer(String name, Duration duration, String... tags) {
        if (!metricsEnabled) return;
        
        try {
            getTimer(name, "Timer metric").record(duration);
            logger.trace("Recorded timer: {} with duration: {}", name, duration);
        } catch (Exception e) {
            logger.warn("Failed to record timer: {}", name, e);
        }
    }

    /**
     * Times an operation and records the duration.
     * 
     * @param name The metric name
     * @param operation The operation to time
     * @return The result of the operation
     */
    public <T> T timeOperation(String name, TimedOperation<T> operation) {
        if (!metricsEnabled) {
            try {
                return operation.execute();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            T result = operation.execute();
            sample.stop(getTimer(name, "Timed operation"));
            return result;
        } catch (Exception e) {
            sample.stop(getTimer(name + ".error", "Failed timed operation"));
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the Prometheus metrics as a string.
     * 
     * @return Prometheus formatted metrics
     */
    public String getPrometheusMetrics() {
        if (!metricsEnabled || !(meterRegistry instanceof PrometheusMeterRegistry)) {
            return "# Metrics disabled\n";
        }
        
        return ((PrometheusMeterRegistry) meterRegistry).scrape();
    }

    private Counter getCounter(String name, String description) {
        return counters.computeIfAbsent(name, key -> 
            Counter.builder(key)
                .description(description)
                .register(meterRegistry)
        );
    }

    private Timer getTimer(String name, String description) {
        return timers.computeIfAbsent(name, key ->
            Timer.builder(key)
                .description(description)
                .register(meterRegistry)
        );
    }

    @FunctionalInterface
    public interface TimedOperation<T> {
        T execute() throws Exception;
    }
}
