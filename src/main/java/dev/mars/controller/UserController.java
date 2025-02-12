// src/main/java/dev/mars/controller/UserController.java
package dev.mars.controller;

import dev.mars.dao.model.User;
import dev.mars.service.UserService;
import io.javalin.http.Context;
import java.util.List;
import java.util.Optional;

public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public void getUserById(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        User user = userService.getUserById(id);
        if (user != null) {
            ctx.json(user);
        } else {
            ctx.status(404);
        }
    }

    public void getAllUsers(Context ctx) {
        List<User> users = userService.getAllUsers();
        ctx.json(users);
    }

    public void addUser(Context ctx) {
        User user = ctx.bodyAsClass(User.class);
        userService.addUser(user);
        ctx.status(201);
    }

    public void updateUser(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        User user = ctx.bodyAsClass(User.class);
        user.setId(id);
        userService.updateUser(user);
        ctx.status(204);
    }

    public void deleteUser(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        userService.deleteUser(id);
        ctx.status(204);
    }

    public void getUsersPaginated(Context ctx) {
        int page = Integer.parseInt(Optional.ofNullable(ctx.queryParam("page")).orElse("1"));
        int size = Integer.parseInt(Optional.ofNullable(ctx.queryParam("size")).orElse("10"));
        List<User> users = userService.getUsersPaginated(page, size);
        ctx.json(users);
    }
}
