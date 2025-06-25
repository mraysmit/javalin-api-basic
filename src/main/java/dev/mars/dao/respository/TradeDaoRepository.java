package dev.mars.dao.respository;

import com.google.inject.Inject;
import dev.mars.dao.model.Trade;
import dev.mars.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TradeDaoRepository implements TradeDao {
    private static final Logger logger = LoggerFactory.getLogger(TradeDaoRepository.class);
    private final DataSource dataSource;

    @Inject
    public TradeDaoRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Trade getTradeById(int id) {
        logger.debug("Getting trade by id: {}", id);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM trades WHERE id = ?")) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToTrade(rs);
            }
        } catch (SQLException e) {
            logger.error("Error getting trade by id", e);
            throw DatabaseException.forOperation("getTradeById", e);
        }
        return null;
    }

    @Override
    public List<Trade> getAllTrades() {
        logger.debug("Getting all trades");
        List<Trade> trades = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM trades")) {
            while (rs.next()) {
                trades.add(mapResultSetToTrade(rs));
            }
        } catch (SQLException e) {
            logger.error("Error getting all trades", e);
            throw DatabaseException.forOperation("getAllTrades", e);
        }
        return trades;
    }

    @Override
    public void addTrade(Trade trade) {
        logger.debug("Adding trade: {}", trade.getSymbol());
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO trades (symbol, quantity, price, type, status, trade_date, settlement_date, counterparty, notes) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            stmt.setString(1, trade.getSymbol());
            stmt.setInt(2, trade.getQuantity());
            stmt.setDouble(3, trade.getPrice());
            stmt.setString(4, trade.getType());
            stmt.setString(5, trade.getStatus());
            stmt.setDate(6, trade.getTradeDate() != null ? Date.valueOf(trade.getTradeDate()) : null);
            stmt.setDate(7, trade.getSettlementDate() != null ? Date.valueOf(trade.getSettlementDate()) : null);
            stmt.setString(8, trade.getCounterparty());
            stmt.setString(9, trade.getNotes());
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error adding trade", e);
            throw DatabaseException.forOperation("addTrade", e);
        }
    }

    @Override
    public void updateTrade(Trade trade) {
        logger.debug("Updating trade with id: {}", trade.getId());
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE trades SET symbol = ?, quantity = ?, price = ?, type = ?, status = ?, " +
                             "trade_date = ?, settlement_date = ?, counterparty = ?, notes = ? WHERE id = ?")) {
            stmt.setString(1, trade.getSymbol());
            stmt.setInt(2, trade.getQuantity());
            stmt.setDouble(3, trade.getPrice());
            stmt.setString(4, trade.getType());
            stmt.setString(5, trade.getStatus());
            stmt.setDate(6, trade.getTradeDate() != null ? Date.valueOf(trade.getTradeDate()) : null);
            stmt.setDate(7, trade.getSettlementDate() != null ? Date.valueOf(trade.getSettlementDate()) : null);
            stmt.setString(8, trade.getCounterparty());
            stmt.setString(9, trade.getNotes());
            stmt.setInt(10, trade.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error updating trade", e);
            throw DatabaseException.forOperation("updateTrade", e);
        }
    }

    @Override
    public void deleteTrade(int id) {
        logger.debug("Deleting trade with id: {}", id);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM trades WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error deleting trade", e);
            throw DatabaseException.forOperation("deleteTrade", e);
        }
    }

    @Override
    public List<Trade> getTradesPaginated(int offset, int limit) {
        logger.debug("Getting trades paginated: offset={}, limit={}", offset, limit);
        List<Trade> trades = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM trades LIMIT ? OFFSET ?")) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                trades.add(mapResultSetToTrade(rs));
            }
        } catch (SQLException e) {
            logger.error("Error getting trades paginated", e);
            throw DatabaseException.forOperation("getTradesPaginated", e);
        }
        return trades;
    }

    private Trade mapResultSetToTrade(ResultSet rs) throws SQLException {
        return new Trade(
                rs.getInt("id"),
                rs.getString("symbol"),
                rs.getInt("quantity"),
                rs.getDouble("price"),
                rs.getString("type"),
                rs.getString("status"),
                rs.getDate("trade_date") != null ? rs.getDate("trade_date").toLocalDate() : null,
                rs.getDate("settlement_date") != null ? rs.getDate("settlement_date").toLocalDate() : null,
                rs.getString("counterparty"),
                rs.getString("notes")
        );
    }
}