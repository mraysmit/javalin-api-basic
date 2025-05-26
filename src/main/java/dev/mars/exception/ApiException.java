package dev.mars.exception;

/**
 * Base exception class for API-related exceptions.
 * This class serves as the parent for all custom exceptions in the API.
 */
public class ApiException extends RuntimeException {
    
    private final int statusCode;
    
    /**
     * Creates a new API exception with the specified message and status code.
     * 
     * @param message The error message
     * @param statusCode The HTTP status code
     */
    public ApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
    
    /**
     * Creates a new API exception with the specified message, cause, and status code.
     * 
     * @param message The error message
     * @param cause The cause of the exception
     * @param statusCode The HTTP status code
     */
    public ApiException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }
    
    /**
     * Gets the HTTP status code associated with this exception.
     * 
     * @return The HTTP status code
     */
    public int getStatusCode() {
        return statusCode;
    }
}