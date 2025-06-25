package dev.mars.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Optional;

/**
 * Configuration loader that supports YAML files and environment variable overrides.
 */
public class ConfigurationLoader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationLoader.class);
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    
    /**
     * Loads application configuration from YAML file with environment variable overrides.
     * 
     * @return ApplicationProperties instance
     */
    public static ApplicationProperties loadConfiguration() {
        logger.info("Loading application configuration");
        
        ApplicationProperties properties = loadFromYaml();
        applyEnvironmentOverrides(properties);
        
        logger.info("Configuration loaded successfully");
        return properties;
    }
    
    private static ApplicationProperties loadFromYaml() {
        String configFile = System.getProperty("config.file", "application.yml");
        logger.debug("Loading configuration from: {}", configFile);
        
        try (InputStream inputStream = ConfigurationLoader.class.getClassLoader()
                .getResourceAsStream(configFile)) {
            
            if (inputStream == null) {
                logger.warn("Configuration file {} not found, using defaults", configFile);
                return new ApplicationProperties();
            }
            
            ApplicationProperties properties = yamlMapper.readValue(inputStream, ApplicationProperties.class);
            logger.debug("Successfully loaded configuration from YAML");
            return properties;
            
        } catch (Exception e) {
            logger.error("Failed to load configuration from YAML, using defaults", e);
            return new ApplicationProperties();
        }
    }
    
    private static void applyEnvironmentOverrides(ApplicationProperties properties) {
        logger.debug("Applying environment variable overrides");
        
        // Server configuration overrides
        getEnvAsInt("SERVER_PORT").ifPresent(properties.getServer()::setPort);
        getEnvAsString("SERVER_HOST").ifPresent(properties.getServer()::setHost);
        getEnvAsString("SERVER_CONTEXT_PATH").ifPresent(properties.getServer()::setContextPath);
        
        // Database configuration overrides
        getEnvAsString("DATABASE_URL").ifPresent(properties.getDatabase()::setUrl);
        getEnvAsString("DATABASE_USERNAME").ifPresent(properties.getDatabase()::setUsername);
        getEnvAsString("DATABASE_PASSWORD").ifPresent(properties.getDatabase()::setPassword);
        getEnvAsString("DATABASE_DRIVER").ifPresent(properties.getDatabase()::setDriverClassName);
        
        // Cache configuration overrides
        getEnvAsBoolean("CACHE_ENABLED").ifPresent(properties.getCache()::setEnabled);
        getEnvAsLong("CACHE_MAX_SIZE").ifPresent(properties.getCache()::setMaxSize);
        getEnvAsLong("CACHE_EXPIRE_MINUTES").ifPresent(properties.getCache()::setExpireAfterWriteMinutes);
        
        // Metrics configuration overrides
        getEnvAsBoolean("METRICS_ENABLED").ifPresent(properties.getMetrics()::setEnabled);
        getEnvAsString("METRICS_ENDPOINT").ifPresent(properties.getMetrics()::setEndpoint);
        
        // API configuration overrides
        getEnvAsString("API_VERSION").ifPresent(properties.getApi()::setVersion);
        getEnvAsBoolean("API_DOCS_ENABLED").ifPresent(properties.getApi().getDocumentation()::setEnabled);
        getEnvAsString("API_DOCS_PATH").ifPresent(properties.getApi().getDocumentation()::setPath);
        
        logger.debug("Environment variable overrides applied");
    }
    
    private static Optional<String> getEnvAsString(String key) {
        return Optional.ofNullable(System.getenv(key))
                .filter(value -> !value.trim().isEmpty());
    }
    
    private static Optional<Integer> getEnvAsInt(String key) {
        return getEnvAsString(key).map(value -> {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid integer value for environment variable {}: {}", key, value);
                return null;
            }
        });
    }
    
    private static Optional<Long> getEnvAsLong(String key) {
        return getEnvAsString(key).map(value -> {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid long value for environment variable {}: {}", key, value);
                return null;
            }
        });
    }
    
    private static Optional<Boolean> getEnvAsBoolean(String key) {
        return getEnvAsString(key).map(value -> {
            return "true".equalsIgnoreCase(value) || "1".equals(value);
        });
    }
}
