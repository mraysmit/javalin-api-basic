package dev.mars.dao.respository;

import dev.mars.dao.model.User;
import java.util.List;

public interface UserDao {
    User getUserById(int id);
    List<User> getAllUsers();
    void addUser(User user);
    void updateUser(User user);
    void deleteUser(int id);
    List<User> getUsersPaginated(int offset, int limit);
}
