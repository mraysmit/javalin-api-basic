package dev.mars.controller;

import dev.mars.dao.model.Trade;
import dev.mars.service.TradeService;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class TradeController {
    private static final Logger logger = LoggerFactory.getLogger(TradeController.class);
    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
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
        int page = Integer.parseInt(Optional.ofNullable(ctx.queryParam("page")).orElse("1"));
        int size = Integer.parseInt(Optional.ofNullable(ctx.queryParam("size")).orElse("10"));
        logger.debug("Fetching trades paginated: page={}, size={}", page, size);
        List<Trade> trades = tradeService.getTradesPaginated(page, size);
        ctx.json(trades);
    }
}