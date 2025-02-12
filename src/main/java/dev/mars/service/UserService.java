package dev.mars.service;


import dev.mars.dao.respository.UserDao;
import dev.mars.dao.model.User;
import java.util.List;

public class UserService {
    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User getUserById(int id) {
        return userDao.getUserById(id);
    }

    public List<User> getAllUsers() {
        return userDao.getAllUsers();
    }

    public void addUser(User user) {
        userDao.addUser(user);
    }

    public void updateUser(User user) {
        userDao.updateUser(user);
    }

    public void deleteUser(int id) {
        userDao.deleteUser(id);
    }

    public List<User> getUsersPaginated(int page, int size) {
        int offset = (page - 1) * size;
        return userDao.getUsersPaginated(offset, size);
    }
}