package dev.mars.routes;

import dev.mars.controller.BaseController;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures general API routes.
 */
public class ApiRoutes {
    private static final Logger logger = LoggerFactory.getLogger(ApiRoutes.class);
    
    /**
     * Registers all general API routes with the Javalin app.
     * 
     * @param app The Javalin app
     * @param baseController The base controller
     */
    public static void register(Javalin app, BaseController baseController) {
        logger.info("Registering API routes");
        
        app.get("/", baseController::handleRoot);
        app.get("/hello/{name}", baseController::handleHello);
        app.get("/query", baseController::handleQueryParams);
        
        logger.info("API routes registered");
    }
}