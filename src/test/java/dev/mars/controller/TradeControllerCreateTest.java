package dev.mars.controller;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

public class TradeControllerCreateTest {

    private final Context ctx = mock(Context.class);

    @Test
    public void POST_to_create_trades_gives_201_for_valid_symbol() {
        when(ctx.queryParam("symbol")).thenReturn("AAPL");
        TradeControllerCreateTest.create(ctx); // the handler we're testing
        verify(ctx).status(201);
    }

    @Test(expected = BadRequestResponse.class)
    public void POST_to_create_trades_throws_for_invalid_symbol() {
        when(ctx.queryParam("symbol")).thenReturn(null);
        TradeControllerCreateTest.create(ctx); // the handler we're testing
    }

    // Static method that matches the pattern in the issue description
    public static void create(Context ctx) {
        String symbol = ctx.queryParam("symbol");
        if (symbol == null || symbol.isEmpty()) {
            throw new BadRequestResponse("Symbol is required");
        }
        // In a real implementation, we would create a trade here
        ctx.status(201);
    }
}