package dev.mars.exception;

/**
 * Exception thrown when a user is not found.
 */
public class UserNotFoundException extends ApiException {
    /**
     * Creates a new UserNotFoundException with the specified message.
     * 
     * @param message The error message
     */
    public UserNotFoundException(String message) {
        super(message, 404);
    }
}
