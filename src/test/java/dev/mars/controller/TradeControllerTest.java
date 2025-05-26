package dev.mars.controller;

import dev.mars.config.AppConfig;
import dev.mars.dao.model.Trade;
import dev.mars.routes.ApiRoutes;
import dev.mars.routes.TradeRoutes;
import dev.mars.routes.UserRoutes;
import io.javalin.Javalin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests for the TradeController.
 * These tests use HTTP requests to test the endpoints directly.
 */
public class TradeControllerTest {

    private static final int TEST_PORT = 7073; // Using a different port to avoid conflicts
    private static final String BASE_URL = "http://localhost:" + TEST_PORT;
    private Javalin app;

    @Before
    public void setup() throws IOException, InterruptedException {
        // Start the server directly in the same JVM
        System.out.println("[DEBUG_LOG] Starting server directly in the same JVM");

        // Create the Javalin app
        this.app = Javalin.create().start(TEST_PORT);

        // Initialize application configuration
        System.out.println("[DEBUG_LOG] Initializing application configuration");
        AppConfig appConfig = new AppConfig();

        // Register routes
        System.out.println("[DEBUG_LOG] Registering routes");
        ApiRoutes.register(app, appConfig.getBaseController());
        UserRoutes.register(app, appConfig.getUserController());
        TradeRoutes.register(app, appConfig.getTradeController());

        // Register exception handlers
        System.out.println("[DEBUG_LOG] Registering exception handlers");
        dev.mars.exception.ExceptionHandler.register(app);

        System.out.println("[DEBUG_LOG] Server started on port " + TEST_PORT);

        // Wait a moment for the server to initialize
        Thread.sleep(1000);

        // Verify server is running
        try {
            System.out.println("[DEBUG_LOG] Checking if server is running...");
            URL url = new URL(BASE_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            System.out.println("[DEBUG_LOG] Server response code: " + responseCode);
            if (responseCode != 200) {
                throw new RuntimeException("Server not running properly. Status code: " + responseCode);
            }
        } catch (Exception e) {
            System.err.println("[DEBUG_LOG] Error connecting to server: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to start server", e);
        }
    }

    @After
    public void teardown() {
        // Stop the Javalin app
        if (app != null) {
            System.out.println("[DEBUG_LOG] Stopping Javalin app");
            app.stop();
        }
    }

    @Test
    public void testGetTradeById_NotFound() throws IOException {
        // Create connection
        URL url = new URL(BASE_URL + "/trades/999");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Get response
        int responseCode = connection.getResponseCode();

        // Verify response
        assertEquals(404, responseCode);
    }

    @Test
    public void testGetAllTrades() throws IOException {
        // Create connection
        URL url = new URL(BASE_URL + "/trades");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Get response
        int responseCode = connection.getResponseCode();
        String responseBody = getResponseBody(connection);

        // Verify response
        assertEquals(200, responseCode);
        // The response should be a JSON array, which starts with [ and ends with ]
        assertTrue(responseBody.startsWith("[") && responseBody.endsWith("]"));
    }

    @Test
    public void testAddTrade() throws IOException {
        // Create connection
        URL url = new URL(BASE_URL + "/trades");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Create a JSON representation of a trade
        String jsonBody = "{" +
                "\"symbol\":\"AAPL\"," +
                "\"quantity\":100," +
                "\"price\":150.50," +
                "\"type\":\"BUY\"," +
                "\"status\":\"PENDING\"," +
                "\"tradeDate\":\"" + LocalDate.now() + "\"," +
                "\"settlementDate\":\"" + LocalDate.now().plusDays(2) + "\"," +
                "\"counterparty\":\"Broker XYZ\"," +
                "\"notes\":\"Test trade\"" +
                "}";

        // Send request body
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Get response
        int responseCode = connection.getResponseCode();

        // Verify response
        assertEquals(201, responseCode);
    }

    @Test
    public void testAddAndGetTrade() throws IOException {
        // First, add a trade
        URL addUrl = new URL(BASE_URL + "/trades");
        HttpURLConnection addConnection = (HttpURLConnection) addUrl.openConnection();
        addConnection.setRequestMethod("POST");
        addConnection.setRequestProperty("Content-Type", "application/json");
        addConnection.setDoOutput(true);

        // Create a JSON representation of a trade
        String jsonBody = "{" +
                "\"symbol\":\"GOOG\"," +
                "\"quantity\":50," +
                "\"price\":2500.75," +
                "\"type\":\"BUY\"," +
                "\"status\":\"PENDING\"," +
                "\"tradeDate\":\"" + LocalDate.now() + "\"," +
                "\"settlementDate\":\"" + LocalDate.now().plusDays(2) + "\"," +
                "\"counterparty\":\"Broker ABC\"," +
                "\"notes\":\"Google stock purchase\"" +
                "}";

        // Send request body
        try (OutputStream os = addConnection.getOutputStream()) {
            byte[] input = jsonBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Verify add response
        int addResponseCode = addConnection.getResponseCode();
        assertEquals(201, addResponseCode);

        // Then, get all trades to find the added trade
        URL getAllUrl = new URL(BASE_URL + "/trades");
        HttpURLConnection getAllConnection = (HttpURLConnection) getAllUrl.openConnection();
        getAllConnection.setRequestMethod("GET");

        // Get response
        int getAllResponseCode = getAllConnection.getResponseCode();
        String getAllResponseBody = getResponseBody(getAllConnection);

        // Verify response
        assertEquals(200, getAllResponseCode);
        assertTrue(getAllResponseBody.contains("GOOG"));
        assertTrue(getAllResponseBody.contains("2500.75"));
        assertTrue(getAllResponseBody.contains("Google stock purchase"));
    }

    @Test
    public void testUpdateTrade() throws IOException {
        // First, add a trade
        URL addUrl = new URL(BASE_URL + "/trades");
        HttpURLConnection addConnection = (HttpURLConnection) addUrl.openConnection();
        addConnection.setRequestMethod("POST");
        addConnection.setRequestProperty("Content-Type", "application/json");
        addConnection.setDoOutput(true);

        // Create a JSON representation of a trade
        String jsonBody = "{" +
                "\"symbol\":\"MSFT\"," +
                "\"quantity\":75," +
                "\"price\":300.25," +
                "\"type\":\"BUY\"," +
                "\"status\":\"PENDING\"," +
                "\"tradeDate\":\"" + LocalDate.now() + "\"," +
                "\"settlementDate\":\"" + LocalDate.now().plusDays(2) + "\"," +
                "\"counterparty\":\"Broker DEF\"," +
                "\"notes\":\"Original trade\"" +
                "}";

        // Send request body
        try (OutputStream os = addConnection.getOutputStream()) {
            byte[] input = jsonBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Verify add response
        int addResponseCode = addConnection.getResponseCode();
        assertEquals(201, addResponseCode);

        // Get all trades to find the ID of the added trade
        URL getAllUrl = new URL(BASE_URL + "/trades");
        HttpURLConnection getAllConnection = (HttpURLConnection) getAllUrl.openConnection();
        getAllConnection.setRequestMethod("GET");

        // Get response
        String getAllResponseBody = getResponseBody(getAllConnection);

        // Extract the ID from the response
        int startIndex = getAllResponseBody.indexOf("\"id\":");
        int endIndex = getAllResponseBody.indexOf(",", startIndex);
        String idStr = getAllResponseBody.substring(startIndex + 5, endIndex).trim();
        int tradeId = Integer.parseInt(idStr);

        // Update the trade
        URL updateUrl = new URL(BASE_URL + "/trades/" + tradeId);
        HttpURLConnection updateConnection = (HttpURLConnection) updateUrl.openConnection();
        updateConnection.setRequestMethod("PUT");
        updateConnection.setRequestProperty("Content-Type", "application/json");
        updateConnection.setDoOutput(true);

        // Create a JSON representation of the updated trade
        String updateJsonBody = "{" +
                "\"symbol\":\"MSFT\"," +
                "\"quantity\":100," +
                "\"price\":305.50," +
                "\"type\":\"BUY\"," +
                "\"status\":\"EXECUTED\"," +
                "\"tradeDate\":\"" + LocalDate.now() + "\"," +
                "\"settlementDate\":\"" + LocalDate.now().plusDays(2) + "\"," +
                "\"counterparty\":\"Broker DEF\"," +
                "\"notes\":\"Updated trade\"" +
                "}";

        // Send request body
        try (OutputStream os = updateConnection.getOutputStream()) {
            byte[] input = updateJsonBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Verify update response
        int updateResponseCode = updateConnection.getResponseCode();
        assertEquals(204, updateResponseCode);

        // Get the trade to verify the update
        URL getTradeUrl = new URL(BASE_URL + "/trades/" + tradeId);
        HttpURLConnection getTradeConnection = (HttpURLConnection) getTradeUrl.openConnection();
        getTradeConnection.setRequestMethod("GET");

        // Get response
        int getTradeResponseCode = getTradeConnection.getResponseCode();
        String getTradeResponseBody = getResponseBody(getTradeConnection);

        // Verify response
        assertEquals(200, getTradeResponseCode);
        assertTrue(getTradeResponseBody.contains("EXECUTED"));
        assertTrue(getTradeResponseBody.contains("305.5"));
        assertTrue(getTradeResponseBody.contains("Updated trade"));
    }

    @Test
    public void testDeleteTrade() throws IOException {
        // First, add a trade
        URL addUrl = new URL(BASE_URL + "/trades");
        HttpURLConnection addConnection = (HttpURLConnection) addUrl.openConnection();
        addConnection.setRequestMethod("POST");
        addConnection.setRequestProperty("Content-Type", "application/json");
        addConnection.setDoOutput(true);

        // Create a JSON representation of a trade
        String jsonBody = "{" +
                "\"symbol\":\"AMZN\"," +
                "\"quantity\":25," +
                "\"price\":3500.00," +
                "\"type\":\"BUY\"," +
                "\"status\":\"PENDING\"," +
                "\"tradeDate\":\"" + LocalDate.now() + "\"," +
                "\"settlementDate\":\"" + LocalDate.now().plusDays(2) + "\"," +
                "\"counterparty\":\"Broker GHI\"," +
                "\"notes\":\"Trade to delete\"" +
                "}";

        // Send request body
        try (OutputStream os = addConnection.getOutputStream()) {
            byte[] input = jsonBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Verify add response
        int addResponseCode = addConnection.getResponseCode();
        assertEquals(201, addResponseCode);

        // Get all trades to find the ID of the added trade
        URL getAllUrl = new URL(BASE_URL + "/trades");
        HttpURLConnection getAllConnection = (HttpURLConnection) getAllUrl.openConnection();
        getAllConnection.setRequestMethod("GET");

        // Get response
        String getAllResponseBody = getResponseBody(getAllConnection);

        // Extract the ID from the response
        int startIndex = getAllResponseBody.indexOf("\"id\":");
        int endIndex = getAllResponseBody.indexOf(",", startIndex);
        String idStr = getAllResponseBody.substring(startIndex + 5, endIndex).trim();
        int tradeId = Integer.parseInt(idStr);

        // Delete the trade
        URL deleteUrl = new URL(BASE_URL + "/trades/" + tradeId);
        HttpURLConnection deleteConnection = (HttpURLConnection) deleteUrl.openConnection();
        deleteConnection.setRequestMethod("DELETE");

        // Verify delete response
        int deleteResponseCode = deleteConnection.getResponseCode();
        assertEquals(204, deleteResponseCode);

        // Try to get the deleted trade
        URL getTradeUrl = new URL(BASE_URL + "/trades/" + tradeId);
        HttpURLConnection getTradeConnection = (HttpURLConnection) getTradeUrl.openConnection();
        getTradeConnection.setRequestMethod("GET");

        // Verify response
        int getTradeResponseCode = getTradeConnection.getResponseCode();
        assertEquals(404, getTradeResponseCode);
    }

    @Test
    public void testGetTradesPaginated() throws IOException {
        // Add multiple trades
        for (int i = 0; i < 5; i++) {
            URL addUrl = new URL(BASE_URL + "/trades");
            HttpURLConnection addConnection = (HttpURLConnection) addUrl.openConnection();
            addConnection.setRequestMethod("POST");
            addConnection.setRequestProperty("Content-Type", "application/json");
            addConnection.setDoOutput(true);

            // Create a JSON representation of a trade
            String jsonBody = "{" +
                    "\"symbol\":\"STOCK" + i + "\"," +
                    "\"quantity\":" + (100 + i) + "," +
                    "\"price\":" + (150.50 + i) + "," +
                    "\"type\":\"BUY\"," +
                    "\"status\":\"PENDING\"," +
                    "\"tradeDate\":\"" + LocalDate.now() + "\"," +
                    "\"settlementDate\":\"" + LocalDate.now().plusDays(2) + "\"," +
                    "\"counterparty\":\"Broker XYZ\"," +
                    "\"notes\":\"Paginated Trade " + i + "\"" +
                    "}";

            // Send request body
            try (OutputStream os = addConnection.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Verify add response
            int addResponseCode = addConnection.getResponseCode();
            assertEquals(201, addResponseCode);
        }

        // Get paginated trades
        URL paginatedUrl = new URL(BASE_URL + "/trades/paginated?page=1&size=3");
        HttpURLConnection paginatedConnection = (HttpURLConnection) paginatedUrl.openConnection();
        paginatedConnection.setRequestMethod("GET");

        // Get response
        int paginatedResponseCode = paginatedConnection.getResponseCode();
        String paginatedResponseBody = getResponseBody(paginatedConnection);

        // Verify response
        assertEquals(200, paginatedResponseCode);

        // The response should be a JSON array with at most 3 trades
        int count = 0;
        int index = 0;
        while ((index = paginatedResponseBody.indexOf("\"id\":", index + 1)) != -1) {
            count++;
        }
        assertTrue(count <= 3);
    }

    private String getResponseBody(HttpURLConnection connection) throws IOException {
        try (Scanner scanner = new Scanner(connection.getInputStream(), "UTF-8")) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        } catch (IOException e) {
            try (Scanner scanner = new Scanner(connection.getErrorStream(), "UTF-8")) {
                scanner.useDelimiter("\\A");
                return scanner.hasNext() ? scanner.next() : "";
            }
        }
    }
}
