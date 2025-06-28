package dev.mars.controller;

import com.google.inject.Inject;
import dev.mars.dao.model.User;
import dev.mars.dto.PageRequest;
import dev.mars.dto.PageResponse;
import dev.mars.service.UserService;
import dev.mars.service.cache.CacheService;
import dev.mars.service.metrics.MetricsService;
import dev.mars.service.validation.ValidationService;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final ValidationService validationService;
    private final MetricsService metricsService;
    private final CacheService cacheService;

    @Inject
    public UserController(UserService userService, ValidationService validationService,
                         MetricsService metricsService, CacheService cacheService) {
        this.userService = userService;
        this.validationService = validationService;
        this.metricsService = metricsService;
        this.cacheService = cacheService;
    }

    public void getUserById(Context ctx) {
        Instant start = Instant.now();
        metricsService.incrementCounter("http.requests.total");

        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            logger.debug("Fetching user with id: {}", id);

            // Try cache first
            String cacheKey = "user:" + id;
            Optional<User> cachedUser = cacheService.get(cacheKey, User.class);

            User user;
            if (cachedUser.isPresent()) {
                user = cachedUser.get();
                logger.trace("User {} found in cache", id);
            } else {
                user = userService.getUserById(id);
                cacheService.put(cacheKey, user);
                logger.trace("User {} cached", id);
            }

            ctx.json(user);
            metricsService.recordTimer("http.request.duration", Duration.between(start, Instant.now()));

        } catch (dev.mars.exception.UserNotFoundException e) {
            logger.error("User not found", e);
            metricsService.incrementCounter("http.requests.errors");
            ctx.status(404).json(Map.of("error", "User not found", "message", e.getMessage()));
        } catch (NumberFormatException e) {
            logger.error("Invalid user ID format", e);
            metricsService.incrementCounter("http.requests.errors");
            ctx.status(400).json(Map.of("error", "Invalid ID format", "message", "User ID must be a number"));
        } catch (Exception e) {
            logger.error("Unexpected error fetching user", e);
            metricsService.incrementCounter("http.requests.errors");
            ctx.status(500).json(Map.of("error", "Internal server error"));
        }
    }

    public void getAllUsers(Context ctx) {
        logger.debug("Fetching all users");
        List<User> users = userService.getAllUsers();
        ctx.json(users);
    }

    public void addUser(Context ctx) {
        Instant start = Instant.now();
        metricsService.incrementCounter("http.requests.total");

        try {
            User user = ctx.bodyAsClass(User.class);

            // Validate the user object
            validationService.validate(user);

            logger.debug("Adding user: {}", user.getName());
            userService.addUser(user);

            // Invalidate cache for user lists
            cacheService.evict("users:all");

            metricsService.incrementCounter("users.created");
            metricsService.recordTimer("http.request.duration", Duration.between(start, Instant.now()));
            ctx.status(201).json(Map.of("message", "User created successfully"));

        } catch (ValidationService.ValidationException e) {
            logger.warn("User validation failed", e);
            metricsService.incrementCounter("http.requests.errors");
            ctx.status(400).json(Map.of("error", "Validation failed", "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error adding user", e);
            metricsService.incrementCounter("http.requests.errors");
            ctx.status(500).json(Map.of("error", "Internal server error"));
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

        // Invalidate cache for the deleted user
        cacheService.evict("user:" + id);
        cacheService.evict("users:all");

        ctx.status(204);
    }

    public void getUsersPaginated(Context ctx) {
        Instant start = Instant.now();
        metricsService.incrementCounter("http.requests.total");

        try {
            // Parse and validate pagination parameters
            PageRequest pageRequest = new PageRequest();
            pageRequest.setPage(Integer.parseInt(Optional.ofNullable(ctx.queryParam("page")).orElse("0")));
            pageRequest.setSize(Integer.parseInt(Optional.ofNullable(ctx.queryParam("size")).orElse("20")));
            pageRequest.setSortBy(ctx.queryParam("sortBy"));

            String sortDir = ctx.queryParam("sortDirection");
            if (sortDir != null) {
                pageRequest.setSortDirection(PageRequest.SortDirection.valueOf(sortDir.toUpperCase()));
            }

            validationService.validate(pageRequest);

            logger.debug("Fetching users paginated: {}", pageRequest);

            // Try cache first
            String cacheKey = String.format("users:page:%d:size:%d:sort:%s:%s",
                pageRequest.getPage(), pageRequest.getSize(), pageRequest.getSortBy(), pageRequest.getSortDirection());

            PageResponse<User> response = cacheService.getOrCompute(cacheKey, PageResponse.class, () -> {
                List<User> users = userService.getUsersPaginated(pageRequest.getPage(), pageRequest.getSize());
                long totalUsers = userService.getUserCount();
                return PageResponse.of(users, pageRequest, totalUsers);
            });

            metricsService.recordTimer("http.request.duration", Duration.between(start, Instant.now()));
            ctx.json(response);

        } catch (ValidationService.ValidationException e) {
            logger.warn("Pagination validation failed", e);
            metricsService.incrementCounter("http.requests.errors");
            ctx.status(400).json(Map.of("error", "Validation failed", "message", e.getMessage()));
        } catch (NumberFormatException e) {
            logger.error("Invalid pagination parameters", e);
            metricsService.incrementCounter("http.requests.errors");
            ctx.status(400).json(Map.of("error", "Invalid pagination parameters"));
        } catch (Exception e) {
            logger.error("Error fetching paginated users", e);
            metricsService.incrementCounter("http.requests.errors");
            ctx.status(500).json(Map.of("error", "Internal server error"));
        }
    }
}
