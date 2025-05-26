package dev.mars.routes;

import dev.mars.controller.UserController;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures routes related to user operations.
 */
public class UserRoutes {
    private static final Logger logger = LoggerFactory.getLogger(UserRoutes.class);

    /**
     * Registers all user-related routes with the Javalin app.
     * 
     * @param app The Javalin app
     * @param userController The user controller
     */
    public static void register(Javalin app, UserController userController) {
        logger.info("Registering user routes");

        app.get("/users", userController::getAllUsers);
        app.get("/users/paginated", userController::getUsersPaginated);
        app.get("/users/{id}", userController::getUserById);
        app.post("/users", userController::addUser);
        app.put("/users/{id}", userController::updateUser);
        app.delete("/users/{id}", userController::deleteUser);

        logger.info("User routes registered");
    }
}
