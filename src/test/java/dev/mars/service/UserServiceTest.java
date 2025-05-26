package dev.mars.service;

import dev.mars.dao.model.User;
import dev.mars.dao.respository.UserDao;
import dev.mars.exception.UserNotFoundException;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    private UserDao userDao;
    private UserService userService;

    @Before
    public void setup() {
        userDao = mock(UserDao.class);
        userService = new UserService(userDao);
    }

    @Test
    public void testGetUserById_UserExists() {
        // Arrange
        User expectedUser = new User(1, "John Doe");
        when(userDao.getUserById(1)).thenReturn(expectedUser);

        // Act
        User actualUser = userService.getUserById(1);

        // Assert
        assertSame(expectedUser, actualUser);
        verify(userDao).getUserById(1);
    }

    @Test(expected = UserNotFoundException.class)
    public void testGetUserById_UserNotFound() {
        // Arrange
        when(userDao.getUserById(1)).thenReturn(null);

        // Act
        userService.getUserById(1);

        // This should throw UserNotFoundException
    }

    @Test
    public void testGetAllUsers() {
        // Arrange
        List<User> expectedUsers = Arrays.asList(
            new User(1, "John Doe"),
            new User(2, "Jane Smith")
        );
        when(userDao.getAllUsers()).thenReturn(expectedUsers);

        // Act
        List<User> actualUsers = userService.getAllUsers();

        // Assert
        assertSame(expectedUsers, actualUsers);
        verify(userDao).getAllUsers();
    }

    @Test
    public void testAddUser() {
        // Arrange
        User user = new User(1, "John Doe");

        // Act
        userService.addUser(user);

        // Assert
        verify(userDao).addUser(user);
    }

    @Test
    public void testUpdateUser() {
        // Arrange
        User user = new User(1, "John Doe");

        // Act
        userService.updateUser(user);

        // Assert
        verify(userDao).updateUser(user);
    }

    @Test
    public void testDeleteUser() {
        // Arrange
        int userId = 1;

        // Act
        userService.deleteUser(userId);

        // Assert
        verify(userDao).deleteUser(userId);
    }

    @Test
    public void testGetUsersPaginated() {
        // Arrange
        int page = 2;
        int size = 10;
        int offset = (page - 1) * size;
        List<User> expectedUsers = Arrays.asList(
            new User(11, "User 11"),
            new User(12, "User 12")
        );
        when(userDao.getUsersPaginated(offset, size)).thenReturn(expectedUsers);

        // Act
        List<User> actualUsers = userService.getUsersPaginated(page, size);

        // Assert
        assertSame(expectedUsers, actualUsers);
        verify(userDao).getUsersPaginated(offset, size);
    }
}
