package dev.mars.exception;

/**
 * Exception thrown when a trade is not found.
 */
public class TradeNotFoundException extends ApiException {
    /**
     * Creates a new TradeNotFoundException with the specified message.
     *
     * @param message The error message
     */
    public TradeNotFoundException(String message) {
        super(message, 404);
    }
}