package dev.mars.exception;

import io.javalin.Javalin;
import io.javalin.http.HttpResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    public static void register(Javalin app) {
        app.exception(ApiException.class, (e, ctx) -> {
            logger.error("API exception: {}", e.getMessage(), e);
            ctx.status(e.getStatusCode()).json(new ErrorResponse(e.getMessage()));
        });

        app.exception(HttpResponseException.class, (e, ctx) -> {
            logger.error("HTTP response error", e);
            ctx.status(e.getStatus()).json(new ErrorResponse(e.getMessage()));
        });

        app.exception(Exception.class, (e, ctx) -> {
            logger.error("Internal server error", e);
            ctx.status(500).json(new ErrorResponse("Internal server error"));
        });
    }

    public static class ErrorResponse {
        private final String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
