package dev.mars.routes;

import dev.mars.controller.TradeController;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures routes related to trade operations.
 */
public class TradeRoutes {
    private static final Logger logger = LoggerFactory.getLogger(TradeRoutes.class);

    /**
     * Registers all trade-related routes with the Javalin app.
     *
     * @param app The Javalin app
     * @param tradeController The trade controller
     */
    public static void register(Javalin app, TradeController tradeController) {
        logger.info("Registering trade routes");

        app.get("/trades", tradeController::getAllTrades);
        app.get("/trades/paginated", tradeController::getTradesPaginated);
        app.get("/trades/{id}", tradeController::getTradeById);
        app.post("/trades", tradeController::addTrade);
        app.put("/trades/{id}", tradeController::updateTrade);
        app.delete("/trades/{id}", tradeController::deleteTrade);

        logger.info("Trade routes registered");
    }
}