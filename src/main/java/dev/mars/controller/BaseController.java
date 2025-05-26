package dev.mars.controller;

import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for handling basic API routes.
 */
public class BaseController {
    private static final Logger logger = LoggerFactory.getLogger(BaseController.class);
    
    /**
     * Handles the root endpoint.
     * 
     * @param ctx The Javalin context
     */
    public void handleRoot(Context ctx) {
        logger.debug("Handling root endpoint");
        ctx.result("Hello, World!");
    }
    
    /**
     * Handles the hello endpoint with a name parameter.
     * 
     * @param ctx The Javalin context
     */
    public void handleHello(Context ctx) {
        String name = ctx.pathParam("name");
        logger.debug("Handling hello endpoint for name: {}", name);
        ctx.result("Hello, " + name + "!");
    }
    
    /**
     * Handles query parameters.
     * 
     * @param ctx The Javalin context
     */
    public void handleQueryParams(Context ctx) {
        String param1 = ctx.queryParam("param1");
        String param2 = ctx.queryParam("param2");
        logger.debug("Handling query parameters: param1={}, param2={}", param1, param2);
        ctx.result("Query parameters received: param1 = " + param1 + ", param2 = " + param2);
    }
}