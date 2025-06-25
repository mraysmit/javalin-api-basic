package dev.mars.controller;

import dev.mars.dao.model.Trade;
import dev.mars.exception.TradeNotFoundException;
import dev.mars.service.TradeService;
import dev.mars.service.cache.CacheService;
import dev.mars.service.metrics.MetricsService;
import dev.mars.service.validation.ValidationService;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

public class TradeControllerMockTest {

    private final Context ctx = mock(Context.class);
    private TradeService tradeService;
    private ValidationService validationService;
    private MetricsService metricsService;
    private CacheService cacheService;
    private TradeController tradeController;

    @Before
    public void setup() {
        tradeService = mock(TradeService.class);
        validationService = mock(ValidationService.class);
        metricsService = mock(MetricsService.class);
        cacheService = mock(CacheService.class);
        tradeController = new TradeController(tradeService, validationService, metricsService, cacheService);

        // Setup default mock behaviors
        when(cacheService.get(anyString(), any())).thenReturn(Optional.empty());
    }

    @Test
    public void testGetTradeById_Success() {
        // Arrange
        int tradeId = 1;
        Trade mockTrade = new Trade(tradeId, "AAPL", 100, 150.5, "BUY", "PENDING", 
                LocalDate.now(), LocalDate.now().plusDays(2), "Broker XYZ", "Test trade");

        when(ctx.pathParam("id")).thenReturn(String.valueOf(tradeId));
        when(tradeService.getTradeById(tradeId)).thenReturn(mockTrade);

        // Act
        tradeController.getTradeById(ctx);

        // Assert
        verify(ctx).json(mockTrade);
        verify(ctx, never()).status(404);
    }

    @Test
    public void testGetTradeById_NotFound() {
        // Arrange
        int tradeId = 999;
        when(ctx.pathParam("id")).thenReturn(String.valueOf(tradeId));
        when(tradeService.getTradeById(tradeId)).thenThrow(new TradeNotFoundException("Trade not found"));

        // Act
        tradeController.getTradeById(ctx);

        // Assert
        verify(ctx).status(404);
        verify(ctx, never()).json(any());
    }

    @Test
    public void testGetAllTrades() {
        // Arrange
        List<Trade> mockTrades = new ArrayList<>();
        mockTrades.add(new Trade(1, "AAPL", 100, 150.5, "BUY", "PENDING", 
                LocalDate.now(), LocalDate.now().plusDays(2), "Broker XYZ", "Test trade 1"));
        mockTrades.add(new Trade(2, "GOOG", 50, 2500.75, "BUY", "EXECUTED", 
                LocalDate.now(), LocalDate.now().plusDays(2), "Broker ABC", "Test trade 2"));

        when(tradeService.getAllTrades()).thenReturn(mockTrades);

        // Act
        tradeController.getAllTrades(ctx);

        // Assert
        verify(ctx).json(mockTrades);
    }

    @Test
    public void testAddTrade_Success() {
        // Arrange
        Trade mockTrade = new Trade(0, "AAPL", 100, 150.5, "BUY", "PENDING", 
                LocalDate.now(), LocalDate.now().plusDays(2), "Broker XYZ", "Test trade");

        when(ctx.bodyAsClass(Trade.class)).thenReturn(mockTrade);

        // Act
        tradeController.addTrade(ctx);

        // Assert
        verify(tradeService).addTrade(mockTrade);
        verify(ctx).status(201);
    }

    @Test
    public void testAddTrade_Error() {
        // Arrange
        when(ctx.bodyAsClass(Trade.class)).thenThrow(new RuntimeException("Invalid trade data"));
        when(ctx.status(500)).thenReturn(ctx); // Mock the chained method call

        // Act
        tradeController.addTrade(ctx);

        // Assert
        verify(ctx).status(500);
        verify(ctx).result("Invalid trade data");
    }

    @Test
    public void testUpdateTrade() {
        // Arrange
        int tradeId = 1;
        Trade mockTrade = new Trade(0, "AAPL", 100, 150.5, "BUY", "PENDING", 
                LocalDate.now(), LocalDate.now().plusDays(2), "Broker XYZ", "Test trade");

        when(ctx.pathParam("id")).thenReturn(String.valueOf(tradeId));
        when(ctx.bodyAsClass(Trade.class)).thenReturn(mockTrade);

        // Act
        tradeController.updateTrade(ctx);

        // Assert
        verify(ctx).status(204);

        // Verify that the ID was set on the trade
        Trade expectedTrade = new Trade(tradeId, "AAPL", 100, 150.5, "BUY", "PENDING", 
                LocalDate.now(), LocalDate.now().plusDays(2), "Broker XYZ", "Test trade");
        verify(tradeService).updateTrade(Mockito.argThat(trade -> trade.getId() == tradeId));
    }

    @Test
    public void testDeleteTrade() {
        // Arrange
        int tradeId = 1;
        when(ctx.pathParam("id")).thenReturn(String.valueOf(tradeId));

        // Act
        tradeController.deleteTrade(ctx);

        // Assert
        verify(tradeService).deleteTrade(tradeId);
        verify(ctx).status(204);
    }

    @Test
    public void testGetTradesPaginated_WithParams() {
        // Arrange
        int page = 2;
        int size = 5;
        List<Trade> mockTrades = new ArrayList<>();
        mockTrades.add(new Trade(6, "AAPL", 100, 150.5, "BUY", "PENDING", 
                LocalDate.now(), LocalDate.now().plusDays(2), "Broker XYZ", "Test trade 6"));
        mockTrades.add(new Trade(7, "GOOG", 50, 2500.75, "BUY", "EXECUTED", 
                LocalDate.now(), LocalDate.now().plusDays(2), "Broker ABC", "Test trade 7"));

        when(ctx.queryParam("page")).thenReturn(String.valueOf(page));
        when(ctx.queryParam("size")).thenReturn(String.valueOf(size));
        when(tradeService.getTradesPaginated(page, size)).thenReturn(mockTrades);

        // Act
        tradeController.getTradesPaginated(ctx);

        // Assert
        verify(ctx).json(mockTrades);
    }

    @Test
    public void testGetTradesPaginated_DefaultParams() {
        // Arrange
        int defaultPage = 1;
        int defaultSize = 10;
        List<Trade> mockTrades = new ArrayList<>();
        mockTrades.add(new Trade(1, "AAPL", 100, 150.5, "BUY", "PENDING", 
                LocalDate.now(), LocalDate.now().plusDays(2), "Broker XYZ", "Test trade 1"));

        when(ctx.queryParam("page")).thenReturn(null);
        when(ctx.queryParam("size")).thenReturn(null);
        when(tradeService.getTradesPaginated(defaultPage, defaultSize)).thenReturn(mockTrades);

        // Act
        tradeController.getTradesPaginated(ctx);

        // Assert
        verify(ctx).json(mockTrades);
    }

    @Test(expected = NumberFormatException.class)
    public void testGetTradeById_InvalidId() {
        // Arrange
        when(ctx.pathParam("id")).thenReturn("invalid");

        // Act
        tradeController.getTradeById(ctx);

        // Assert - exception expected
    }
}
