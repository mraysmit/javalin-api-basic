package dev.mars.service;

import com.google.inject.Inject;
import dev.mars.dao.model.Trade;
import dev.mars.dao.respository.TradeDao;
import dev.mars.exception.TradeNotFoundException;

import java.util.List;

public class TradeService {
    private final TradeDao tradeDaoRepo;

    @Inject
    public TradeService(TradeDao tradeDaoRepo) {
        this.tradeDaoRepo = tradeDaoRepo;
    }

    public Trade getTradeById(int id) {
        Trade trade = tradeDaoRepo.getTradeById(id);
        if (trade == null) {
            throw new TradeNotFoundException("Trade not found with id: " + id);
        }
        return trade;
    }

    public List<Trade> getAllTrades() {
        return tradeDaoRepo.getAllTrades();
    }

    public void addTrade(Trade trade) {
        tradeDaoRepo.addTrade(trade);
    }

    public void updateTrade(Trade trade) {
        tradeDaoRepo.updateTrade(trade);
    }

    public void deleteTrade(int id) {
        tradeDaoRepo.deleteTrade(id);
    }

    public List<Trade> getTradesPaginated(int page, int size) {
        int offset = page * size;
        return tradeDaoRepo.getTradesPaginated(offset, size);
    }

    public long getTradeCount() {
        return tradeDaoRepo.getAllTrades().size(); // Simple implementation for now
    }
}