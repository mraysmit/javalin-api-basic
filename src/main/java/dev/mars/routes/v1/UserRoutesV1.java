package dev.mars.routes.v1;

import dev.mars.controller.UserController;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Version 1 user routes.
 */
public class UserRoutesV1 {
    private static final Logger logger = LoggerFactory.getLogger(UserRoutesV1.class);
    private static final String API_VERSION = "/api/v1";

    /**
     * Registers all user-related routes for API version 1.
     * 
     * @param app The Javalin app
     * @param userController The user controller
     */
    public static void register(Javalin app, UserController userController) {
        logger.info("Registering user routes v1");

        app.get(API_VERSION + "/users", userController::getAllUsers);
        app.get(API_VERSION + "/users/paginated", userController::getUsersPaginated);
        app.get(API_VERSION + "/users/{id}", userController::getUserById);
        app.post(API_VERSION + "/users", userController::addUser);
        app.put(API_VERSION + "/users/{id}", userController::updateUser);
        app.delete(API_VERSION + "/users/{id}", userController::deleteUser);

        logger.info("User routes v1 registered");
    }
}
