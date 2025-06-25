package dev.mars.routes.v1;

import dev.mars.controller.TradeController;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Version 1 trade routes.
 */
public class TradeRoutesV1 {
    private static final Logger logger = LoggerFactory.getLogger(TradeRoutesV1.class);
    private static final String API_VERSION = "/api/v1";

    /**
     * Registers all trade-related routes for API version 1.
     *
     * @param app The Javalin app
     * @param tradeController The trade controller
     */
    public static void register(Javalin app, TradeController tradeController) {
        logger.info("Registering trade routes v1");

        app.get(API_VERSION + "/trades", tradeController::getAllTrades);
        app.get(API_VERSION + "/trades/paginated", tradeController::getTradesPaginated);
        app.get(API_VERSION + "/trades/{id}", tradeController::getTradeById);
        app.post(API_VERSION + "/trades", tradeController::addTrade);
        app.put(API_VERSION + "/trades/{id}", tradeController::updateTrade);
        app.delete(API_VERSION + "/trades/{id}", tradeController::deleteTrade);

        logger.info("Trade routes v1 registered");
    }
}
