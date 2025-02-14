package dev.mars;

import dev.mars.RouteHandlers;
import dev.mars.controller.UserController;
import dev.mars.dao.respository.UserDaoRepository;
import dev.mars.service.UserService;
import io.javalin.Javalin;
import org.h2.jdbcx.JdbcDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Routes {
    private static final Logger logger = LoggerFactory.getLogger(Routes.class);

    public static void configure(Javalin app) {
        logger.info("Configuring routes");

        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        UserDaoRepository userDaoRepository = new UserDaoRepository(dataSource);
        UserService userService = new UserService(userDaoRepository);
        UserController userController = new UserController(userService);

        app.get("/", RouteHandlers::handleRoot);
        app.get("/hello/{name}", RouteHandlers::handleHello);
        app.get("/query", RouteHandlers::handleQueryParams);

        app.get("/users", userController::getAllUsers);
        app.get("/users/{id}", userController::getUserById);
        app.post("/users", userController::addUser);
        app.put("/users/{id}", userController::updateUser);
        app.delete("/users/{id}", userController::deleteUser);
        app.get("/users/paginated", userController::getUsersPaginated);

        logger.info("Routes configured");
    }
}