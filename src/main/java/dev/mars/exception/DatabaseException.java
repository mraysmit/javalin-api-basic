package dev.mars.exception;

import java.sql.SQLException;

/**
 * Exception thrown when a database operation fails.
 */
public class DatabaseException extends ApiException {
    
    /**
     * Creates a new DatabaseException with the specified message.
     * 
     * @param message The error message
     */
    public DatabaseException(String message) {
        super(message, 500);
    }
    
    /**
     * Creates a new DatabaseException with the specified message and cause.
     * 
     * @param message The error message
     * @param cause The cause of the exception
     */
    public DatabaseException(String message, SQLException cause) {
        super(message, cause, 500);
    }
    
    /**
     * Creates a new DatabaseException for a specific operation.
     * 
     * @param operation The database operation that failed
     * @param cause The cause of the exception
     * @return A new DatabaseException
     */
    public static DatabaseException forOperation(String operation, SQLException cause) {
        return new DatabaseException("Database error during " + operation + ": " + cause.getMessage(), cause);
    }
}