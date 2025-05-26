package dev.mars.dao.respository;

import dev.mars.dao.model.User;
import dev.mars.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class UserDaoRepository implements UserDao {
    private static final Logger logger = LoggerFactory.getLogger(UserDaoRepository.class);
    private final DataSource dataSource;

    public UserDaoRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public User getUserById(int id) {
        logger.debug("Getting user by id: {}", id);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE id = ?")) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("id"), rs.getString("name"));
            }
        } catch (SQLException e) {
            logger.error("Error getting user by id", e);
            throw DatabaseException.forOperation("getUserById", e);
        }
        return null;
    }

    @Override
    public List<User> getAllUsers() {
        logger.debug("Getting all users");
        List<User> users = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {
            while (rs.next()) {
                users.add(new User(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            logger.error("Error getting all users", e);
            throw DatabaseException.forOperation("getAllUsers", e);
        }
        return users;
    }

    @Override
    public void addUser(User user) {
        logger.debug("Adding user: {}", user.getName());
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (name) VALUES (?)")) {
            stmt.setString(1, user.getName());
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error adding user", e);
            throw DatabaseException.forOperation("addUser", e);
        }
    }

    @Override
    public void updateUser(User user) {
        logger.debug("Updating user with id: {}", user.getId());
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE users SET name = ? WHERE id = ?")) {
            stmt.setString(1, user.getName());
            stmt.setInt(2, user.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error updating user", e);
            throw DatabaseException.forOperation("updateUser", e);
        }
    }

    @Override
    public void deleteUser(int id) {
        logger.debug("Deleting user with id: {}", id);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error deleting user", e);
            throw DatabaseException.forOperation("deleteUser", e);
        }
    }

    @Override
    public List<User> getUsersPaginated(int offset, int limit) {
        logger.debug("Getting users paginated: offset={}, limit={}", offset, limit);
        List<User> users = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users LIMIT ? OFFSET ?")) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(new User(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            logger.error("Error getting users paginated", e);
            throw DatabaseException.forOperation("getUsersPaginated", e);
        }
        return users;
    }
}
