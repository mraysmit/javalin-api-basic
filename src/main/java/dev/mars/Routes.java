package dev.mars;

import dev.mars.controller.UserController;
import dev.mars.dao.respository.UserDaoRepository;
import dev.mars.service.UserService;
import io.javalin.Javalin;
import org.h2.jdbcx.JdbcDataSource;

public class Routes {

    public static void configure(Javalin app) {
        app.get("/", RouteHandlers::handleRoot);
        app.get("/hello/{name}", RouteHandlers::handleHello);
        app.get("/query", RouteHandlers::handleQueryParams);
        // Add more routes here

        JdbcDataSource dataSource = new org.h2.jdbcx.JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        UserDaoRepository userDao = new UserDaoRepository(dataSource);
        UserService userService = new UserService(userDao);
        UserController userController = new UserController(userService);

        app.get("/users/:id", userController::getUserById);
        app.get("/users", userController::getAllUsers);
        app.post("/users", userController::addUser);
        app.put("/users/:id", userController::updateUser);
        app.delete("/users/:id", userController::deleteUser);
        app.get("/users/paginated", userController::getUsersPaginated);
    }
}