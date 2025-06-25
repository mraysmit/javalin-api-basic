package dev.mars.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Application configuration properties loaded from YAML.
 */
public class ApplicationProperties {
    
    @JsonProperty("server")
    private ServerConfig server = new ServerConfig();
    
    @JsonProperty("database")
    private DatabaseConfig database = new DatabaseConfig();
    
    @JsonProperty("cache")
    private CacheConfig cache = new CacheConfig();
    
    @JsonProperty("metrics")
    private MetricsConfig metrics = new MetricsConfig();
    
    @JsonProperty("api")
    private ApiConfig api = new ApiConfig();

    // Getters and setters
    public ServerConfig getServer() { return server; }
    public void setServer(ServerConfig server) { this.server = server; }
    
    public DatabaseConfig getDatabase() { return database; }
    public void setDatabase(DatabaseConfig database) { this.database = database; }
    
    public CacheConfig getCache() { return cache; }
    public void setCache(CacheConfig cache) { this.cache = cache; }
    
    public MetricsConfig getMetrics() { return metrics; }
    public void setMetrics(MetricsConfig metrics) { this.metrics = metrics; }
    
    public ApiConfig getApi() { return api; }
    public void setApi(ApiConfig api) { this.api = api; }

    public static class ServerConfig {
        @JsonProperty("port")
        private int port = 8080;
        
        @JsonProperty("host")
        private String host = "0.0.0.0";
        
        @JsonProperty("context-path")
        private String contextPath = "/api";

        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        
        public String getContextPath() { return contextPath; }
        public void setContextPath(String contextPath) { this.contextPath = contextPath; }
    }

    public static class DatabaseConfig {
        @JsonProperty("url")
        private String url = "jdbc:h2:mem:testdb";
        
        @JsonProperty("username")
        private String username = "sa";
        
        @JsonProperty("password")
        private String password = "";
        
        @JsonProperty("driver-class-name")
        private String driverClassName = "org.h2.Driver";

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getDriverClassName() { return driverClassName; }
        public void setDriverClassName(String driverClassName) { this.driverClassName = driverClassName; }
    }

    public static class CacheConfig {
        @JsonProperty("enabled")
        private boolean enabled = true;
        
        @JsonProperty("max-size")
        private long maxSize = 1000;
        
        @JsonProperty("expire-after-write-minutes")
        private long expireAfterWriteMinutes = 30;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        public long getMaxSize() { return maxSize; }
        public void setMaxSize(long maxSize) { this.maxSize = maxSize; }
        
        public long getExpireAfterWriteMinutes() { return expireAfterWriteMinutes; }
        public void setExpireAfterWriteMinutes(long expireAfterWriteMinutes) { this.expireAfterWriteMinutes = expireAfterWriteMinutes; }
    }

    public static class MetricsConfig {
        @JsonProperty("enabled")
        private boolean enabled = true;
        
        @JsonProperty("endpoint")
        private String endpoint = "/metrics";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    }

    public static class ApiConfig {
        @JsonProperty("version")
        private String version = "v1";
        
        @JsonProperty("documentation")
        private DocumentationConfig documentation = new DocumentationConfig();

        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        
        public DocumentationConfig getDocumentation() { return documentation; }
        public void setDocumentation(DocumentationConfig documentation) { this.documentation = documentation; }

        public static class DocumentationConfig {
            @JsonProperty("enabled")
            private boolean enabled = true;
            
            @JsonProperty("path")
            private String path = "/swagger-ui";

            public boolean isEnabled() { return enabled; }
            public void setEnabled(boolean enabled) { this.enabled = enabled; }
            
            public String getPath() { return path; }
            public void setPath(String path) { this.path = path; }
        }
    }
}
