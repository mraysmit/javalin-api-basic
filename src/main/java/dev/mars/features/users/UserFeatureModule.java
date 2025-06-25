package dev.mars.features.users;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import dev.mars.controller.UserController;
import dev.mars.dao.respository.UserDao;
import dev.mars.dao.respository.UserDaoRepository;
import dev.mars.service.UserService;

/**
 * Guice module for user feature dependencies.
 */
public class UserFeatureModule extends AbstractModule {
    
    @Override
    protected void configure() {
        // Bind user-specific dependencies
        bind(UserDao.class).to(UserDaoRepository.class).in(Singleton.class);
        bind(UserService.class).in(Singleton.class);
        bind(UserController.class).in(Singleton.class);
    }
}
