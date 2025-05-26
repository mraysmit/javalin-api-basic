package dev.mars.service;

import dev.mars.dao.model.Trade;
import dev.mars.dao.respository.TradeDao;
import dev.mars.exception.TradeNotFoundException;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;

public class TradeServiceTest {

    private TradeDao tradeDao;
    private TradeService tradeService;

    @Before
    public void setup() {
        tradeDao = mock(TradeDao.class);
        tradeService = new TradeService(tradeDao);
    }

    @Test
    public void testGetTradeById_TradeExists() {
        // Arrange
        Trade expectedTrade = createSampleTrade(1);
        when(tradeDao.getTradeById(1)).thenReturn(expectedTrade);

        // Act
        Trade actualTrade = tradeService.getTradeById(1);

        // Assert
        assertSame(expectedTrade, actualTrade);
        verify(tradeDao).getTradeById(1);
    }

    @Test(expected = TradeNotFoundException.class)
    public void testGetTradeById_TradeNotFound() {
        // Arrange
        when(tradeDao.getTradeById(1)).thenReturn(null);

        // Act
        tradeService.getTradeById(1);

        // This should throw TradeNotFoundException
    }

    @Test
    public void testGetAllTrades() {
        // Arrange
        List<Trade> expectedTrades = Arrays.asList(
            createSampleTrade(1),
            createSampleTrade(2)
        );
        when(tradeDao.getAllTrades()).thenReturn(expectedTrades);

        // Act
        List<Trade> actualTrades = tradeService.getAllTrades();

        // Assert
        assertSame(expectedTrades, actualTrades);
        verify(tradeDao).getAllTrades();
    }

    @Test
    public void testAddTrade() {
        // Arrange
        Trade trade = createSampleTrade(1);

        // Act
        tradeService.addTrade(trade);

        // Assert
        verify(tradeDao).addTrade(trade);
    }

    @Test
    public void testUpdateTrade() {
        // Arrange
        Trade trade = createSampleTrade(1);

        // Act
        tradeService.updateTrade(trade);

        // Assert
        verify(tradeDao).updateTrade(trade);
    }

    @Test
    public void testDeleteTrade() {
        // Arrange
        int tradeId = 1;

        // Act
        tradeService.deleteTrade(tradeId);

        // Assert
        verify(tradeDao).deleteTrade(tradeId);
    }

    @Test
    public void testGetTradesPaginated() {
        // Arrange
        int page = 2;
        int size = 10;
        int offset = (page - 1) * size;
        List<Trade> expectedTrades = Arrays.asList(
            createSampleTrade(11),
            createSampleTrade(12)
        );
        when(tradeDao.getTradesPaginated(offset, size)).thenReturn(expectedTrades);

        // Act
        List<Trade> actualTrades = tradeService.getTradesPaginated(page, size);

        // Assert
        assertSame(expectedTrades, actualTrades);
        verify(tradeDao).getTradesPaginated(offset, size);
    }

    /**
     * Creates a sample trade for testing.
     *
     * @param id The ID to set
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