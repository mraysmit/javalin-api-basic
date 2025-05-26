package dev.mars;

import dev.mars.config.AppConfig;
import dev.mars.exception.ExceptionHandler;
import dev.mars.routes.ApiRoutes;
import dev.mars.routes.TradeRoutes;
import dev.mars.routes.UserRoutes;
import io.javalin.Javalin;

/**
 * A test server that starts Javalin on a specified port.
 * This is used by the integration tests to avoid the SO_REUSEPORT issue.
 */
public class TestServer {
    public static void main(String[] args) {
        // Get the port from command-line arguments
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 7070;

        System.out.println("Starting test server on port " + port);

        // Create and start the Javalin app
        var app = Javalin.create().start(port);

        // Initialize application configuration
        AppConfig appConfig = new AppConfig();

        // Register routes
        ApiRoutes.register(app, appConfig.getBaseController());
        UserRoutes.register(app, appConfig.getUserController());
        TradeRoutes.register(app, appConfig.getTradeController());

        // Register exception handlers
        ExceptionHandler.register(app);

        System.out.println("Test server started on port " + port);

        // Add a shutdown hook to stop the server when the JVM exits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Stopping test server");
            app.stop();
        }));
    }
}
