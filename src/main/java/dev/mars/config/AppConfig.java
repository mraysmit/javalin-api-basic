package dev.mars.config;

import dev.mars.controller.BaseController;
import dev.mars.controller.TradeController;
import dev.mars.controller.UserController;
import dev.mars.dao.respository.TradeDao;
import dev.mars.dao.respository.TradeDaoRepository;
import dev.mars.dao.respository.UserDao;
import dev.mars.dao.respository.UserDaoRepository;
import dev.mars.service.TradeService;
import dev.mars.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

/**
 * Configuration class for application components and dependency injection.
 */
public class AppConfig {
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    private final DataSource dataSource;
    private final UserDao userDao;
    private final UserService userService;
    private final UserController userController;
    private final BaseController baseController;

    // New Trade components
    private final TradeDao tradeDao;
    private final TradeService tradeService;
    private final TradeController tradeController;

    /**
     * Creates a new AppConfig instance with all application components.
     */
    public AppConfig() {
        logger.info("Initializing application configuration");

        // Initialize database
        this.dataSource = DatabaseConfig.createDataSource();
        DatabaseConfig.initializeDatabase(dataSource);

        // Initialize User components with dependencies
        this.userDao = new UserDaoRepository(dataSource);
        this.userService = new UserService(userDao);
        this.userController = new UserController(userService);
        this.baseController = new BaseController();

        // Initialize Trade components with dependencies
        this.tradeDao = new TradeDaoRepository(dataSource);
        this.tradeService = new TradeService(tradeDao);
        this.tradeController = new TradeController(tradeService);

        logger.info("Application configuration initialized");
    }

    /**
     * Gets the user controller.
     *
     * @return The user controller
     */
    public UserController getUserController() {
        return userController;
    }

    /**
     * Gets the user service.
     *
     * @return The user service
     */
    public UserService getUserService() {
        return userService;
    }

    /**
     * Gets the user DAO.
     *
     * @return The user DAO
     */
    public UserDao getUserDao() {
        return userDao;
    }

    /**
     * Gets the data source.
     *
     * @return The data source
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * Gets the base controller.
     *
     * @return The base controller
     */
    public BaseController getBaseController() {
        return baseController;
    }

    /**
     * Gets the trade controller.
     *
     * @return The trade controller
     */
    public TradeController getTradeController() {
        return tradeController;
    }

    /**
     * Gets the trade service.
     *
     * @return The trade service
     */
    public TradeService getTradeService() {
        return tradeService;
    }

    /**
     * Gets the trade DAO.
     *
     * @return The trade DAO
     */
    public TradeDao getTradeDao() {
        return tradeDao;
    }
}