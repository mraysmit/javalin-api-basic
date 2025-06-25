package dev.mars.controller;

import com.google.inject.Inject;
import dev.mars.dao.model.Trade;
import dev.mars.dto.PageRequest;
import dev.mars.dto.PageResponse;
import dev.mars.service.TradeService;
import dev.mars.service.cache.CacheService;
import dev.mars.service.metrics.MetricsService;
import dev.mars.service.validation.ValidationService;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TradeController {
    private static final Logger logger = LoggerFactory.getLogger(TradeController.class);
    private final TradeService tradeService;
    private final ValidationService validationService;
    private final MetricsService metricsService;
    private final CacheService cacheService;

    @Inject
    public TradeController(TradeService tradeService, ValidationService validationService,
                          MetricsService metricsService, CacheService cacheService) {
        this.tradeService = tradeService;
        this.validationService = validationService;
        this.metricsService = metricsService;
        this.cacheService = cacheService;
    }

    public void getTradeById(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        logger.debug("Fetching trade with id: {}", id);
        try {
            Trade trade = tradeService.getTradeById(id);
            ctx.json(trade);
        } catch (dev.mars.exception.TradeNotFoundException e) {
            logger.error("Trade not found", e);
            ctx.status(404);
        }
    }

    public void getAllTrades(Context ctx) {
        logger.debug("Fetching all trades");
        List<Trade> trades = tradeService.getAllTrades();
        ctx.json(trades);
    }

    public void addTrade(Context ctx) {
        try {
            Trade trade = ctx.bodyAsClass(Trade.class);
            logger.debug("Adding trade: {}", trade.getSymbol());
            tradeService.addTrade(trade);
            ctx.status(201);
        } catch (Exception e) {
            logger.error("Error adding trade", e);
            ctx.status(500).result(e.getMessage());
        }
    }

    public void updateTrade(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Trade trade = ctx.bodyAsClass(Trade.class);
        trade.setId(id);
        logger.debug("Updating trade with id: {}", id);
        tradeService.updateTrade(trade);
        ctx.status(204);
    }

    public void deleteTrade(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        logger.debug("Deleting trade with id: {}", id);
        tradeService.deleteTrade(id);
        ctx.status(204);
    }

    public void getTradesPaginated(Context ctx) {
        Instant start = Instant.now();
        metricsService.incrementCounter("http.requests.total");

        try {
            // Parse and validate pagination parameters
            PageRequest pageRequest = new PageRequest();
            pageRequest.setPage(Integer.parseInt(Optional.ofNullable(ctx.queryParam("page")).orElse("0")));
            pageRequest.setSize(Integer.parseInt(Optional.ofNullable(ctx.queryParam("size")).orElse("20")));
            pageRequest.setSortBy(ctx.queryParam("sortBy"));

            String sortDir = ctx.queryParam("sortDirection");
            if (sortDir != null) {
                pageRequest.setSortDirection(PageRequest.SortDirection.valueOf(sortDir.toUpperCase()));
            }

            validationService.validate(pageRequest);

            logger.debug("Fetching trades paginated: {}", pageRequest);

            // Try cache first
            String cacheKey = String.format("trades:page:%d:size:%d:sort:%s:%s",
                pageRequest.getPage(), pageRequest.getSize(), pageRequest.getSortBy(), pageRequest.getSortDirection());

            PageResponse<Trade> response = cacheService.getOrCompute(cacheKey, PageResponse.class, () -> {
                List<Trade> trades = tradeService.getTradesPaginated(pageRequest.getPage(), pageRequest.getSize());
                long totalTrades = tradeService.getTradeCount();
                return PageResponse.of(trades, pageRequest, totalTrades);
            });

            metricsService.recordTimer("http.request.duration", Duration.between(start, Instant.now()));
            ctx.json(response);

        } catch (ValidationService.ValidationException e) {
            logger.warn("Pagination validation failed", e);
            metricsService.incrementCounter("http.requests.errors");
            ctx.status(400).json(Map.of("error", "Validation failed", "message", e.getMessage()));
        } catch (NumberFormatException e) {
            logger.error("Invalid pagination parameters", e);
            metricsService.incrementCounter("http.requests.errors");
            ctx.status(400).json(Map.of("error", "Invalid pagination parameters"));
        } catch (Exception e) {
            logger.error("Error fetching paginated trades", e);
            metricsService.incrementCounter("http.requests.errors");
            ctx.status(500).json(Map.of("error", "Internal server error"));
        }
    }
}