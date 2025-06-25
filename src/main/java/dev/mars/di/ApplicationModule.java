package dev.mars.di;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import dev.mars.config.ApplicationProperties;
import dev.mars.config.ConfigurationLoader;
import dev.mars.controller.BaseController;
import dev.mars.controller.DocumentationController;
import dev.mars.controller.MetricsController;
import dev.mars.controller.TradeController;
import dev.mars.controller.UserController;
import dev.mars.dao.respository.TradeDao;
import dev.mars.dao.respository.TradeDaoRepository;
import dev.mars.dao.respository.UserDao;
import dev.mars.dao.respository.UserDaoRepository;
import dev.mars.service.TradeService;
import dev.mars.service.UserService;
import dev.mars.service.async.AsyncService;
import dev.mars.service.cache.CacheService;
import dev.mars.service.cache.CaffeineCache;
import dev.mars.service.metrics.MetricsService;
import dev.mars.service.validation.ValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

/**
 * Guice module for dependency injection configuration.
 */
public class ApplicationModule extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationModule.class);

    @Override
    protected void configure() {
        logger.info("Configuring dependency injection");
        
        // Bind interfaces to implementations
        bind(UserDao.class).to(UserDaoRepository.class).in(Singleton.class);
        bind(TradeDao.class).to(TradeDaoRepository.class).in(Singleton.class);
        bind(CacheService.class).to(CaffeineCache.class).in(Singleton.class);
        
        // Bind services
        bind(UserService.class).in(Singleton.class);
        bind(TradeService.class).in(Singleton.class);
        bind(ValidationService.class).in(Singleton.class);
        bind(MetricsService.class).in(Singleton.class);
        
        // Bind controllers
        bind(BaseController.class).in(Singleton.class);
        bind(UserController.class).in(Singleton.class);
        bind(TradeController.class).in(Singleton.class);
        bind(MetricsController.class).in(Singleton.class);
        bind(DocumentationController.class).in(Singleton.class);

        // Bind async service
        bind(AsyncService.class).in(Singleton.class);

        logger.info("Dependency injection configuration completed");
    }

    @Provides
    @Singleton
    public ApplicationProperties provideApplicationProperties() {
        return ConfigurationLoader.loadConfiguration();
    }

    @Provides
    @Singleton
    public DataSource provideDataSource(ApplicationProperties properties) {
        logger.info("Creating DataSource with configuration");
        return createDataSource(properties.getDatabase());
    }

    private DataSource createDataSource(ApplicationProperties.DatabaseConfig config) {
        // Create H2 DataSource
        org.h2.jdbcx.JdbcDataSource dataSource = new org.h2.jdbcx.JdbcDataSource();
        dataSource.setURL(config.getUrl());
        dataSource.setUser(config.getUsername());
        dataSource.setPassword(config.getPassword());
        
        // Initialize database schema
        initializeDatabase(dataSource);
        
        return dataSource;
    }

    private void initializeDatabase(DataSource dataSource) {
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            
            // Create users table
            statement.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    email VARCHAR(255),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Create trades table
            statement.execute("""
                CREATE TABLE IF NOT EXISTS trades (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    symbol VARCHAR(10) NOT NULL,
                    quantity INT NOT NULL,
                    price DECIMAL(10,2) NOT NULL,
                    trade_type VARCHAR(10) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            logger.info("Database schema initialized successfully");
            
        } catch (Exception e) {
            logger.error("Failed to initialize database schema", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }
}
