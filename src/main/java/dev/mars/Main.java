package dev.mars;

import com.google.inject.Guice;
import com.google.inject.Injector;
import dev.mars.config.ApplicationProperties;
import dev.mars.controller.BaseController;
import dev.mars.controller.DocumentationController;
import dev.mars.controller.MetricsController;
import dev.mars.controller.TradeController;
import dev.mars.controller.UserController;
import dev.mars.di.ApplicationModule;
import dev.mars.exception.ExceptionHandler;
import dev.mars.routes.v1.TradeRoutesV1;
import dev.mars.routes.v1.UserRoutesV1;
import dev.mars.routes.UserRoutes;
import dev.mars.routes.TradeRoutes;
import dev.mars.service.async.AsyncService;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.json.JavalinJackson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enhanced main application class for the Javalin API.
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static AsyncService asyncService;

    public static void main(String[] args) {
        logger.info("Starting enhanced Javalin API application");

        try {
            // Initialize dependency injection
            Injector injector = Guice.createInjector(new ApplicationModule());

            // Get configuration
            ApplicationProperties properties = injector.getInstance(ApplicationProperties.class);

            // Get port from command line args or configuration
            int port = getPort(args, properties);
            logger.info("Using port: {}", port);

            // Create Javalin app with enhanced configuration
            var app = createJavalinApp(properties).start(port);

            // Get controllers from injector
            BaseController baseController = injector.getInstance(BaseController.class);
            UserController userController = injector.getInstance(UserController.class);
            TradeController tradeController = injector.getInstance(TradeController.class);
            MetricsController metricsController = injector.getInstance(MetricsController.class);
            DocumentationController documentationController = injector.getInstance(DocumentationController.class);

            // Register versioned routes
            registerRoutes(app, baseController, userController, tradeController, metricsController, documentationController, properties);

            // Register exception handlers
            ExceptionHandler.register(app);

            // Initialize async service
            asyncService = injector.getInstance(AsyncService.class);

            // Add shutdown hook
            addShutdownHook();

            logger.info("Enhanced Javalin API application started successfully on port {}", port);

        } catch (Exception e) {
            logger.error("Failed to start application", e);
            System.exit(1);
        }
    }

    private static int getPort(String[] args, ApplicationProperties properties) {
        // Try to get port from command line args first
        if (args.length > 0) {
            try {
                return Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                logger.warn("Invalid port number in command line args: {}. Using configuration.", args[0]);
            }
        }

        // Use port from configuration
        return properties.getServer().getPort();
    }

    private static Javalin createJavalinApp(ApplicationProperties properties) {
        return Javalin.create(config -> {
            // Enable CORS for development
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> {
                    it.allowHost("http://localhost:3000", "http://localhost:8080", "http://127.0.0.1:8080");
                    it.allowCredentials = false; // Set to false for security
                });
            });

            // Enable request logging
            config.bundledPlugins.enableDevLogging();

            // Configure JSON serialization
            config.jsonMapper(new JavalinJackson());

            logger.info("Javalin configured with enhanced features");
        });
    }

    private static void registerRoutes(Javalin app, BaseController baseController,
                                     UserController userController, TradeController tradeController,
                                     MetricsController metricsController, DocumentationController documentationController,
                                     ApplicationProperties properties) {

        String apiVersion = properties.getApi().getVersion();
        logger.info("Registering routes for API version: {}", apiVersion);

        // Health and metrics endpoints
        app.get("/health", metricsController::getHealth);
        app.get(properties.getMetrics().getEndpoint(), metricsController::getMetrics);
        app.get("/cache/stats", metricsController::getCacheStats);

        // API documentation endpoints
        if (properties.getApi().getDocumentation().isEnabled()) {
            app.get("/api-docs", documentationController::getOpenApiSpec);
            app.get(properties.getApi().getDocumentation().getPath(), documentationController::getSwaggerUi);
        }

        // Legacy routes (for backward compatibility)
        app.get("/", baseController::handleRoot);
        app.get("/hello/{name}", baseController::handleHello);
        app.get("/query", baseController::handleQueryParams);

        // Legacy routes (for backward compatibility)
        UserRoutes.register(app, userController);
        TradeRoutes.register(app, tradeController);

        // Versioned API routes
        UserRoutesV1.register(app, userController);
        TradeRoutesV1.register(app, tradeController);

        logger.info("All routes registered successfully");
    }

    private static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down application gracefully");
            if (asyncService != null) {
                asyncService.shutdown();
            }
            logger.info("Application shutdown completed");
        }));
    }
}
