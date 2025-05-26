package dev.mars.dao.respository;

import dev.mars.dao.model.Trade;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for the TradeDaoRepository class.
 * These tests use a real H2 in-memory database to test the repository directly.
 */
public class TradeDaoRepositoryTest {

    private TradeDaoRepository tradeDaoRepository;
    private JdbcDataSource dataSource;

    @Before
    public void setup() throws SQLException {
        // Set up the H2 in-memory database
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        // Create the trades table
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS trades");
            stmt.execute("CREATE TABLE trades (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "symbol VARCHAR(20), " +
                    "quantity INT, " +
                    "price DOUBLE, " +
                    "type VARCHAR(10), " +
                    "status VARCHAR(20), " +
                    "trade_date DATE, " +
                    "settlement_date DATE, " +
                    "counterparty VARCHAR(100), " +
                    "notes VARCHAR(500)" +
                    ")");
        }

        // Create the repository
        tradeDaoRepository = new TradeDaoRepository(dataSource);
    }

    @Test
    public void testAddTrade() {
        // Add a trade
        Trade trade = createSampleTrade(0);
        tradeDaoRepository.addTrade(trade);

        // Get all trades
        List<Trade> trades = tradeDaoRepository.getAllTrades();

        // Verify
        assertEquals(1, trades.size());
        assertEquals("AAPL", trades.get(0).getSymbol());
        assertEquals(100, trades.get(0).getQuantity());
        assertEquals(150.50, trades.get(0).getPrice(), 0.001);
        assertEquals("BUY", trades.get(0).getType());
        assertEquals("PENDING", trades.get(0).getStatus());
        assertEquals(LocalDate.now(), trades.get(0).getTradeDate());
        assertEquals(LocalDate.now().plusDays(2), trades.get(0).getSettlementDate());
        assertEquals("Broker XYZ", trades.get(0).getCounterparty());
        assertEquals("Test trade", trades.get(0).getNotes());
    }

    @Test
    public void testGetTradeById() {
        // Add a trade
        Trade trade = createSampleTrade(0);
        tradeDaoRepository.addTrade(trade);

        // Get all trades to find the ID
        List<Trade> trades = tradeDaoRepository.getAllTrades();
        int tradeId = trades.get(0).getId();

        // Get the trade by ID
        Trade retrievedTrade = tradeDaoRepository.getTradeById(tradeId);

        // Verify
        assertNotNull(retrievedTrade);
        assertEquals("AAPL", retrievedTrade.getSymbol());
        assertEquals(100, retrievedTrade.getQuantity());
        assertEquals(150.50, retrievedTrade.getPrice(), 0.001);
    }

    @Test
    public void testGetTradeById_NotFound() {
        // Get a non-existent trade
        Trade trade = tradeDaoRepository.getTradeById(999);

        // Verify
        assertNull(trade);
    }

    @Test
    public void testGetAllTrades() {
        // Add multiple trades
        tradeDaoRepository.addTrade(createSampleTrade(0));
        tradeDaoRepository.addTrade(createSampleTrade(0));

        // Get all trades
        List<Trade> trades = tradeDaoRepository.getAllTrades();

        // Verify
        assertEquals(2, trades.size());
    }

    @Test
    public void testUpdateTrade() {
        // Add a trade
        Trade trade = createSampleTrade(0);
        tradeDaoRepository.addTrade(trade);

        // Get all trades to find the ID
        List<Trade> trades = tradeDaoRepository.getAllTrades();
        int tradeId = trades.get(0).getId();

        // Update the trade
        Trade updatedTrade = new Trade(
                tradeId,
                "MSFT",
                200,
                250.75,
                "SELL",
                "EXECUTED",
                LocalDate.now(),
                LocalDate.now().plusDays(3),
                "Broker ABC",
                "Updated trade"
        );
        tradeDaoRepository.updateTrade(updatedTrade);

        // Get the trade by ID
        Trade retrievedTrade = tradeDaoRepository.getTradeById(tradeId);

        // Verify
        assertNotNull(retrievedTrade);
        assertEquals("MSFT", retrievedTrade.getSymbol());
        assertEquals(200, retrievedTrade.getQuantity());
        assertEquals(250.75, retrievedTrade.getPrice(), 0.001);
        assertEquals("SELL", retrievedTrade.getType());
        assertEquals("EXECUTED", retrievedTrade.getStatus());
        assertEquals("Broker ABC", retrievedTrade.getCounterparty());
        assertEquals("Updated trade", retrievedTrade.getNotes());
    }

    @Test
    public void testDeleteTrade() {
        // Add a trade
        Trade trade = createSampleTrade(0);
        tradeDaoRepository.addTrade(trade);

        // Get all trades to find the ID
        List<Trade> trades = tradeDaoRepository.getAllTrades();
        int tradeId = trades.get(0).getId();

        // Delete the trade
        tradeDaoRepository.deleteTrade(tradeId);

        // Try to get the deleted trade
        Trade retrievedTrade = tradeDaoRepository.getTradeById(tradeId);

        // Verify
        assertNull(retrievedTrade);
    }

    @Test
    public void testGetTradesPaginated() {
        // Add multiple trades
        for (int i = 0; i < 5; i++) {
            Trade trade = new Trade(
                    0,
                    "STOCK" + i,
                    100 + i,
                    150.50 + i,
                    "BUY",
                    "PENDING",
                    LocalDate.now(),
                    LocalDate.now().plusDays(2),
                    "Broker XYZ",
                    "Trade " + i
            );
            tradeDaoRepository.addTrade(trade);
        }

        // Get paginated trades
        List<Trade> trades = tradeDaoRepository.getTradesPaginated(1, 2);

        // Verify
        assertEquals(2, trades.size());
    }

    /**
     * Creates a sample trade for testing.
     *
     * @param id The ID to set (0 for new trades)
     * @return A sample trade
     */
    private Trade createSampleTrade(int id) {
        return new Trade(
                id,
                "AAPL",
                100,
                150.50,
                "BUY",
                "PENDING",
                LocalDate.now(),
                LocalDate.now().plusDays(2),
                "Broker XYZ",
                "Test trade"
        );
    }
}