package dev.mars.controller;

import dev.mars.dao.model.User;
import dev.mars.exception.UserNotFoundException;
import dev.mars.service.UserService;
import dev.mars.service.cache.CacheService;
import dev.mars.service.metrics.MetricsService;
import dev.mars.service.validation.ValidationService;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;

public class UserControllerMockTest {

    private final Context ctx = mock(Context.class);
    private UserService userService;
    private ValidationService validationService;
    private MetricsService metricsService;
    private CacheService cacheService;
    private UserController userController;

    @Before
    public void setup() {
        userService = mock(UserService.class);
        validationService = mock(ValidationService.class);
        metricsService = mock(MetricsService.class);
        cacheService = mock(CacheService.class);
        userController = new UserController(userService, validationService, metricsService, cacheService);

        // Setup default mock behaviors
        when(cacheService.get(anyString(), any())).thenReturn(Optional.empty());
    }

    @Test
    public void testGetUserById_Success() {
        // Arrange
        int userId = 1;
        User mockUser = new User(userId, "John Doe");

        when(ctx.pathParam("id")).thenReturn(String.valueOf(userId));
        when(userService.getUserById(userId)).thenReturn(mockUser);

        // Act
        userController.getUserById(ctx);

        // Assert
        verify(ctx).json(mockUser);
        verify(ctx, never()).status(404);
    }

    @Test
    public void testGetUserById_NotFound() {
        // Arrange
        int userId = 999;
        when(ctx.pathParam("id")).thenReturn(String.valueOf(userId));
        when(userService.getUserById(userId)).thenThrow(new UserNotFoundException("User not found"));
        when(ctx.status(404)).thenReturn(ctx); // Mock the chained method call

        // Act
        userController.getUserById(ctx);

        // Assert
        verify(ctx).status(404);
        verify(ctx).json(Map.of("error", "User not found", "message", "User not found"));
    }

    @Test
    public void testGetAllUsers() {
        // Arrange
        List<User> mockUsers = new ArrayList<>();
        mockUsers.add(new User(1, "John Doe"));
        mockUsers.add(new User(2, "Jane Smith"));

        when(userService.getAllUsers()).thenReturn(mockUsers);

        // Act
        userController.getAllUsers(ctx);

        // Assert
        verify(ctx).json(mockUsers);
    }

    @Test
    public void testAddUser_Success() {
        // Arrange
        User mockUser = new User(0, "John Doe");

        when(ctx.bodyAsClass(User.class)).thenReturn(mockUser);
        when(ctx.status(201)).thenReturn(ctx); // Mock the chained method call

        // Act
        userController.addUser(ctx);

        // Assert
        verify(userService).addUser(mockUser);
        verify(ctx).status(201);
        verify(ctx).json(Map.of("message", "User created successfully"));
    }

    @Test
    public void testAddUser_Error() {
        // Arrange
        when(ctx.bodyAsClass(User.class)).thenThrow(new RuntimeException("Invalid user data"));
        when(ctx.status(500)).thenReturn(ctx); // Mock the chained method call

        // Act
        userController.addUser(ctx);

        // Assert
        verify(ctx).status(500);
        verify(ctx).json(Map.of("error", "Internal server error"));
    }

    @Test
    public void testUpdateUser() {
        // Arrange
        int userId = 1;
        User mockUser = new User(0, "John Doe");

        when(ctx.pathParam("id")).thenReturn(String.valueOf(userId));
        when(ctx.bodyAsClass(User.class)).thenReturn(mockUser);

        // Act
        userController.updateUser(ctx);

        // Assert
        verify(ctx).status(204);

        // Verify that the ID was set on the user
        verify(userService).updateUser(Mockito.argThat(user -> user.getId() == userId));
    }

    @Test
    public void testDeleteUser() {
        // Arrange
        int userId = 1;
        when(ctx.pathParam("id")).thenReturn(String.valueOf(userId));

        // Act
        userController.deleteUser(ctx);

        // Assert
        verify(userService).deleteUser(userId);
        verify(ctx).status(204);
    }

    @Test
    public void testGetUsersPaginated_WithParams() {
        // Arrange
        int page = 2;
        int size = 5;
        List<User> mockUsers = new ArrayList<>();
        mockUsers.add(new User(6, "User 6"));
        mockUsers.add(new User(7, "User 7"));

        when(ctx.queryParam("page")).thenReturn(String.valueOf(page));
        when(ctx.queryParam("size")).thenReturn(String.valueOf(size));
        when(ctx.queryParam("sortBy")).thenReturn(null);
        when(ctx.queryParam("sortDirection")).thenReturn(null);
        when(userService.getUsersPaginated(page, size)).thenReturn(mockUsers);
        when(userService.getUserCount()).thenReturn(10L);

        // Act
        userController.getUsersPaginated(ctx);

        // Assert
        verify(ctx).json(any()); // Verify that json is called with some response
    }

    @Test
    public void testGetUsersPaginated_DefaultParams() {
        // Arrange
        int defaultPage = 0; // Default page is 0, not 1
        int defaultSize = 20; // Default size is 20, not 10
        List<User> mockUsers = new ArrayList<>();
        mockUsers.add(new User(1, "User 1"));

        when(ctx.queryParam("page")).thenReturn(null);
        when(ctx.queryParam("size")).thenReturn(null);
        when(ctx.queryParam("sortBy")).thenReturn(null);
        when(ctx.queryParam("sortDirection")).thenReturn(null);
        when(userService.getUsersPaginated(defaultPage, defaultSize)).thenReturn(mockUsers);
        when(userService.getUserCount()).thenReturn(1L);

        // Act
        userController.getUsersPaginated(ctx);

        // Assert
        verify(ctx).json(any()); // Verify that json is called with some response
    }

    @Test
    public void testGetUserById_InvalidId() {
        // Arrange
        when(ctx.pathParam("id")).thenReturn("invalid");
        when(ctx.status(400)).thenReturn(ctx);

        // Act
        userController.getUserById(ctx);

        // Assert
        verify(ctx).status(400);
        verify(ctx).json(Map.of("error", "Invalid ID format", "message", "User ID must be a number"));
    }

    // Note: The tests for creating users with valid/invalid usernames have been moved to UserControllerCreateTest
    // to follow the exact pattern provided in the issue description.
}
