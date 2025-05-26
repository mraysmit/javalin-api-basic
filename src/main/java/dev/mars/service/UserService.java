package dev.mars.service;


import dev.mars.dao.respository.UserDao;
import dev.mars.dao.model.User;
import dev.mars.exception.UserNotFoundException;

import java.util.List;

public class UserService {
    private final UserDao userDaoRepo;

    public UserService(UserDao userDaoRepo) {
        this.userDaoRepo = userDaoRepo;
    }

    public User getUserById(int id) {
        User user = userDaoRepo.getUserById(id);
        if (user == null) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        return user;
    }

    public List<User> getAllUsers() {
        return userDaoRepo.getAllUsers();
    }

    public void addUser(User user) {
        userDaoRepo.addUser(user);
    }

    public void updateUser(User user) {
        userDaoRepo.updateUser(user);
    }

    public void deleteUser(int id) {
        userDaoRepo.deleteUser(id);
    }

    public List<User> getUsersPaginated(int page, int size) {
        int offset = (page - 1) * size;
        return userDaoRepo.getUsersPaginated(offset, size);
    }
}
