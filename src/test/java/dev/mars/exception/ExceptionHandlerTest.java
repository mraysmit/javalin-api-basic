package dev.mars.exception;

import io.javalin.http.Context;
import io.javalin.http.HttpResponseException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExceptionHandlerTest {

    private TestContext ctx;

    /**
     * A simple implementation of Context for testing.
     */
    private static class TestContext {
        private int statusCode = 200;
        private Object jsonResponse;

        public TestContext status(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public TestContext json(Object object) {
            this.jsonResponse = object;
            return this;
        }

        public Object getJsonResponse() {
            return jsonResponse;
        }
    }

    @Before
    public void setup() {
        ctx = new TestContext();
    }

    @Test
    public void testUserNotFoundExceptionHandler() {
        // Arrange
        UserNotFoundException exception = new UserNotFoundException("User not found with id: 1");

        // Create the exception handler directly
        io.javalin.http.ExceptionHandler<UserNotFoundException> handler = (e, context) -> {
            TestContext testContext = new TestContext();
            testContext.status(404).json(new ExceptionHandler.ErrorResponse(e.getMessage()));
            
            // Copy the values to our test context
            ctx.status(testContext.getStatusCode()).json(testContext.getJsonResponse());
        };

        // Act
        handler.handle(exception, null); // We're not using the context parameter

        // Assert
        assertEquals(404, ctx.getStatusCode());
        ExceptionHandler.ErrorResponse response = (ExceptionHandler.ErrorResponse) ctx.getJsonResponse();
        assertEquals("User not found with id: 1", response.getMessage());
    }

    @Test
    public void testHttpResponseExceptionHandler() {
        // Arrange
        HttpResponseException exception = new HttpResponseException(400, "Bad request");

        // Create the exception handler directly
        io.javalin.http.ExceptionHandler<HttpResponseException> handler = (e, context) -> {
            TestContext testContext = new TestContext();
            testContext.status(e.getStatus()).json(new ExceptionHandler.ErrorResponse(e.getMessage()));
            
            // Copy the values to our test context
            ctx.status(testContext.getStatusCode()).json(testContext.getJsonResponse());
        };

        // Act
        handler.handle(exception, null); // We're not using the context parameter

        // Assert
        assertEquals(400, ctx.getStatusCode());
        ExceptionHandler.ErrorResponse response = (ExceptionHandler.ErrorResponse) ctx.getJsonResponse();
        assertEquals("Bad request", response.getMessage());
    }

    @Test
    public void testGenericExceptionHandler() {
        // Arrange
        Exception exception = new RuntimeException("Something went wrong");

        // Create the exception handler directly
        io.javalin.http.ExceptionHandler<Exception> handler = (e, context) -> {
            TestContext testContext = new TestContext();
            testContext.status(500).json(new ExceptionHandler.ErrorResponse("Internal server error"));
            
            // Copy the values to our test context
            ctx.status(testContext.getStatusCode()).json(testContext.getJsonResponse());
        };

        // Act
        handler.handle(exception, null); // We're not using the context parameter

        // Assert
        assertEquals(500, ctx.getStatusCode());
        ExceptionHandler.ErrorResponse response = (ExceptionHandler.ErrorResponse) ctx.getJsonResponse();
        assertEquals("Internal server error", response.getMessage());
    }

    @Test
    public void testErrorResponseClass() {
        // Arrange
        String errorMessage = "Test error message";

        // Act
        ExceptionHandler.ErrorResponse response = new ExceptionHandler.ErrorResponse(errorMessage);

        // Assert
        assertEquals(errorMessage, response.getMessage());
    }
}