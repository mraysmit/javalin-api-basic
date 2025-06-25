package dev.mars.features.trades;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import dev.mars.controller.TradeController;
import dev.mars.dao.respository.TradeDao;
import dev.mars.dao.respository.TradeDaoRepository;
import dev.mars.service.TradeService;

/**
 * Guice module for trade feature dependencies.
 */
public class TradeFeatureModule extends AbstractModule {
    
    @Override
    protected void configure() {
        // Bind trade-specific dependencies
        bind(TradeDao.class).to(TradeDaoRepository.class).in(Singleton.class);
        bind(TradeService.class).in(Singleton.class);
        bind(TradeController.class).in(Singleton.class);
    }
}
