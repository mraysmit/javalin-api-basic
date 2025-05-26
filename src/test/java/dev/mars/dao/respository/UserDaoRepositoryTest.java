package dev.mars.dao.respository;

import dev.mars.dao.model.User;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for the UserDaoRepository class.
 * These tests use a real H2 in-memory database to test the repository directly.
 */
public class UserDaoRepositoryTest {

    private UserDaoRepository userDaoRepository;
    private JdbcDataSource dataSource;

    @Before
    public void setup() throws SQLException {
        // Set up the H2 in-memory database
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        // Create the users table
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS users");
            stmt.execute("CREATE TABLE users (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255))");
        }

        // Create the repository
        userDaoRepository = new UserDaoRepository(dataSource);
    }

    @Test
    public void testAddUser() {
        // Add a user
        User user = new User(0, "John Doe");
        userDaoRepository.addUser(user);

        // Get all users
        List<User> users = userDaoRepository.getAllUsers();

        // Verify
        assertEquals(1, users.size());
        assertEquals("John Doe", users.get(0).getName());
    }

    @Test
    public void testGetUserById() {
        // Add a user
        User user = new User(0, "John Doe");
        userDaoRepository.addUser(user);

        // Get all users to find the ID
        List<User> users = userDaoRepository.getAllUsers();
        int userId = users.get(0).getId();

        // Get the user by ID
        User retrievedUser = userDaoRepository.getUserById(userId);

        // Verify
        assertNotNull(retrievedUser);
        assertEquals("John Doe", retrievedUser.getName());
    }

    @Test
    public void testGetUserById_NotFound() {
        // Get a non-existent user
        User user = userDaoRepository.getUserById(999);

        // Verify
        assertNull(user);
    }

    @Test
    public void testGetAllUsers() {
        // Add multiple users
        userDaoRepository.addUser(new User(0, "John Doe"));
        userDaoRepository.addUser(new User(0, "Jane Doe"));

        // Get all users
        List<User> users = userDaoRepository.getAllUsers();

        // Verify
        assertEquals(2, users.size());
    }

    @Test
    public void testUpdateUser() {
        // Add a user
        User user = new User(0, "John Doe");
        userDaoRepository.addUser(user);

        // Get all users to find the ID
        List<User> users = userDaoRepository.getAllUsers();
        int userId = users.get(0).getId();

        // Update the user
        User updatedUser = new User(userId, "Jane Doe");
        userDaoRepository.updateUser(updatedUser);

        // Get the user by ID
        User retrievedUser = userDaoRepository.getUserById(userId);

        // Verify
        assertNotNull(retrievedUser);
        assertEquals("Jane Doe", retrievedUser.getName());
    }

    @Test
    public void testDeleteUser() {
        // Add a user
        User user = new User(0, "John Doe");
        userDaoRepository.addUser(user);

        // Get all users to find the ID
        List<User> users = userDaoRepository.getAllUsers();
        int userId = users.get(0).getId();

        // Delete the user
        userDaoRepository.deleteUser(userId);

        // Try to get the deleted user
        User retrievedUser = userDaoRepository.getUserById(userId);

        // Verify
        assertNull(retrievedUser);
    }

    @Test
    public void testGetUsersPaginated() {
        // Add multiple users
        for (int i = 0; i < 5; i++) {
            userDaoRepository.addUser(new User(0, "User " + i));
        }

        // Get paginated users
        List<User> users = userDaoRepository.getUsersPaginated(1, 2);

        // Verify
        assertEquals(2, users.size());
    }
}