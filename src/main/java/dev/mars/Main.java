package dev.mars;

import dev.mars.config.AppConfig;
import dev.mars.exception.ExceptionHandler;
import dev.mars.routes.ApiRoutes;
import dev.mars.routes.TradeRoutes;
import dev.mars.routes.UserRoutes;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

/**
 * Main application class.
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final int DEFAULT_PORT = 7070;

    public static void main(String[] args) {
        logger.info("Starting application");

        // Get port from command line args or config file
        int port = getPort(args);
        logger.info("Using port: {}", port);

        // Create Javalin app
        var app = Javalin.create(/*config*/).start(port);

        // Initialize application configuration
        AppConfig appConfig = new AppConfig();

        // Register routes
        ApiRoutes.register(app, appConfig.getBaseController());
        UserRoutes.register(app, appConfig.getUserController());
        TradeRoutes.register(app, appConfig.getTradeController());

        // Register exception handlers
        ExceptionHandler.register(app);

        logger.info("Application started successfully");
    }

    /**
     * Gets the port number from command line arguments or from the YAML configuration file.
     * If neither is available, returns the default port (7070).
     *
     * @param args Command line arguments
     * @return The port number to use
     */
    private static int getPort(String[] args) {
        // Try to get port from command line args
        if (args.length > 0) {
            try {
                return Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                logger.warn("Invalid port number in command line args: {}. Using configuration file or default.", args[0]);
            }
        }

        // Try to get port from YAML config file
        try {
            Yaml yaml = new Yaml();
            InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("application.yaml");
            if (inputStream != null) {
                Map<String, Object> config = yaml.load(inputStream);
                if (config != null && config.containsKey("server")) {
                    Map<String, Object> serverConfig = (Map<String, Object>) config.get("server");
                    if (serverConfig != null && serverConfig.containsKey("port")) {
                        Object portObj = serverConfig.get("port");
                        if (portObj instanceof Integer) {
                            return (Integer) portObj;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Error reading port from configuration file: {}", e.getMessage());
        }

        // Return default port if neither command line args nor config file is available
        logger.info("Using default port: {}", DEFAULT_PORT);
        return DEFAULT_PORT;
    }
}
