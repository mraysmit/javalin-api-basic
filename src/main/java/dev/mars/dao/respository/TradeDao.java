package dev.mars.dao.respository;

import dev.mars.dao.model.Trade;
import java.util.List;

public interface TradeDao {
    Trade getTradeById(int id);
    List<Trade> getAllTrades();
    void addTrade(Trade trade);
    void updateTrade(Trade trade);
    void deleteTrade(int id);
    List<Trade> getTradesPaginated(int offset, int limit);
}