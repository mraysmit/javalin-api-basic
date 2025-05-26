package dev.mars.config;

import org.h2.jdbcx.JdbcDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Configuration class for database initialization and connection management.
 */
public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    private static final String DB_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    /**
     * Creates and initializes the data source.
     *
     * @return The configured data source
     */
    public static DataSource createDataSource() {
        logger.info("Creating data source");
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL(DB_URL);
        dataSource.setUser(DB_USER);
        dataSource.setPassword(DB_PASSWORD);
        return dataSource;
    }

    /**
     * Initializes the database schema.
     *
     * @param dataSource The data source to use
     */
    public static void initializeDatabase(DataSource dataSource) {
        logger.info("Initializing database schema");
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            // Create users table
            logger.info("Creating users table");
            stmt.execute("DROP TABLE IF EXISTS users");
            stmt.execute("CREATE TABLE users (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255))");
            logger.info("Users table created");

            // Create trades table
            logger.info("Creating trades table");
            stmt.execute("DROP TABLE IF EXISTS trades");
            stmt.execute("CREATE TABLE trades (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "symbol VARCHAR(20), " +
                    "quantity INT, " +
                    "price DOUBLE, " +
                    "type VARCHAR(10), " +
                    "status VARCHAR(20), " +
                    "trade_date DATE, " +
                    "settlement_date DATE, " +
                    "counterparty VARCHAR(100), " +
                    "notes VARCHAR(500)" +
                    ")");
            logger.info("Trades table created");
        } catch (SQLException e) {
            logger.error("Error initializing database", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
}