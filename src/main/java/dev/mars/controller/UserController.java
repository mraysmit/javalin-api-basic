package dev.mars.controller;

import dev.mars.dao.model.User;
import dev.mars.service.UserService;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;

public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public void getUserById(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        logger.debug("Fetching user with id: {}", id);
        User user = userService.getUserById(id);
        if (user != null) {
            ctx.json(user);
        } else {
            ctx.status(404);
        }
    }

    public void getAllUsers(Context ctx) {
        logger.debug("Fetching all users");
        List<User> users = userService.getAllUsers();
        ctx.json(users);
    }

    public void addUser(Context ctx) {
        try {
            User user = ctx.bodyAsClass(User.class);
            logger.debug("Adding user: {}", user.getName());
            userService.addUser(user);
            ctx.status(201);
        } catch (Exception e) {
            logger.error("Error adding user", e);
            ctx.status(500).result(e.getMessage());
        }
    }

    public void updateUser(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        User user = ctx.bodyAsClass(User.class);
        user.setId(id);
        logger.debug("Updating user with id: {}", id);
        userService.updateUser(user);
        ctx.status(204);
    }

    public void deleteUser(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        logger.debug("Deleting user with id: {}", id);
        userService.deleteUser(id);
        ctx.status(204);
    }

    public void getUsersPaginated(Context ctx) {
        int page = Integer.parseInt(Optional.ofNullable(ctx.queryParam("page")).orElse("1"));
        int size = Integer.parseInt(Optional.ofNullable(ctx.queryParam("size")).orElse("10"));
        logger.debug("Fetching users paginated: page={}, size={}", page, size);
        List<User> users = userService.getUsersPaginated(page, size);
        ctx.json(users);
    }
}