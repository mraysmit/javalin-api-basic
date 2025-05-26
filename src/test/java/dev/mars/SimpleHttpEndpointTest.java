package dev.mars;

import dev.mars.dao.model.User;
import dev.mars.controller.UserController;
import dev.mars.dao.respository.UserDaoRepository;
import dev.mars.service.UserService;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests for the API endpoints without using Mockito.
 * These tests use Java's HttpURLConnection to make HTTP requests to the endpoints.
 */
public class SimpleHttpEndpointTest {

    private static final int TEST_PORT = 7070;
    private static final String BASE_URL = "http://localhost:" + TEST_PORT;
    private HttpClient httpClient;
    private Process serverProcess;

    @Before
    public void setup() throws IOException, InterruptedException {
        // Start the main application in a separate process
        ProcessBuilder processBuilder = new ProcessBuilder(
            "java", 
            "-cp", 
            System.getProperty("java.class.path"),
            "dev.mars.Main"
        );
        processBuilder.redirectErrorStream(true);

        // Print debug information
        System.out.println("Starting server process with command: " + String.join(" ", processBuilder.command()));
        System.out.println("Classpath: " + System.getProperty("java.class.path"));

        // Start the process
        serverProcess = processBuilder.start();

        // Read process output for debugging
        new Thread(() -> {
            try (Scanner scanner = new Scanner(serverProcess.getInputStream())) {
                while (scanner.hasNextLine()) {
                    System.out.println("[SERVER] " + scanner.nextLine());
                }
            }
        }).start();

        // Create HTTP client
        httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        // Wait for server to start
        System.out.println("Waiting for server to start...");
        Thread.sleep(5000); // Wait longer

        // Verify server is running
        try {
            System.out.println("Checking if server is running...");
            URL url = new URL(BASE_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            System.out.println("Server response code: " + responseCode);
            if (responseCode != 200) {
                throw new RuntimeException("Server not running properly. Status code: " + responseCode);
            }
        } catch (Exception e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            e.printStackTrace();
            if (serverProcess != null) {
                serverProcess.destroy();
            }
            throw new RuntimeException("Failed to start server", e);
        }
    }

    @After
    public void teardown() {
        // Stop the server process
        if (serverProcess != null) {
            serverProcess.destroy();
        }
    }

    @Test
    public void testRootEndpoint() throws IOException {
        // Create connection
        URL url = new URL(BASE_URL + "/");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Get response
        int responseCode = connection.getResponseCode();
        String responseBody = getResponseBody(connection);

        // Verify response
        assertEquals(200, responseCode);
        assertEquals("Hello, World!", responseBody);
    }

    @Test
    public void testHelloEndpoint() throws IOException {
        // Create connection
        URL url = new URL(BASE_URL + "/hello/John");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Get response
        int responseCode = connection.getResponseCode();
        String responseBody = getResponseBody(connection);

        // Verify response
        assertEquals(200, responseCode);
        assertEquals("Hello, John!", responseBody);
    }

    @Test
    public void testQueryParamsEndpoint() throws IOException {
        // Create connection
        URL url = new URL(BASE_URL + "/query?param1=value1&param2=value2");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Get response
        int responseCode = connection.getResponseCode();
        String responseBody = getResponseBody(connection);

        // Verify response
        assertEquals(200, responseCode);
        assertEquals("Query parameters received: param1 = value1, param2 = value2", responseBody);
    }

    @Test
    public void testGetAllUsers() throws IOException {
        // Create connection
        URL url = new URL(BASE_URL + "/users");
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
    public void testGetUserById_NotFound() throws IOException {
        // Create connection
        URL url = new URL(BASE_URL + "/users/999");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Get response
        int responseCode = connection.getResponseCode();

        // Verify response
        assertEquals(404, responseCode);
    }

    @Test
    public void testAddUser() throws IOException {
        // Create connection
        URL url = new URL(BASE_URL + "/users");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Send request body
        String jsonBody = "{\"name\":\"Test User\"}";
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
    public void testAddAndGetUser() throws IOException {
        // First, add a user
        URL addUrl = new URL(BASE_URL + "/users");
        HttpURLConnection addConnection = (HttpURLConnection) addUrl.openConnection();
        addConnection.setRequestMethod("POST");
        addConnection.setRequestProperty("Content-Type", "application/json");
        addConnection.setDoOutput(true);

        // Send request body
        String jsonBody = "{\"name\":\"John Doe\"}";
        try (OutputStream os = addConnection.getOutputStream()) {
            byte[] input = jsonBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Verify add response
        int addResponseCode = addConnection.getResponseCode();
        assertEquals(201, addResponseCode);

        // Then, get all users to find the added user
        URL getAllUrl = new URL(BASE_URL + "/users");
        HttpURLConnection getAllConnection = (HttpURLConnection) getAllUrl.openConnection();
        getAllConnection.setRequestMethod("GET");

        // Get response
        int getAllResponseCode = getAllConnection.getResponseCode();
        String getAllResponseBody = getResponseBody(getAllConnection);

        // Verify response
        assertEquals(200, getAllResponseCode);
        assertTrue(getAllResponseBody.contains("John Doe"));
    }

    @Test
    public void testUpdateUser() throws IOException {
        // First, add a user
        URL addUrl = new URL(BASE_URL + "/users");
        HttpURLConnection addConnection = (HttpURLConnection) addUrl.openConnection();
        addConnection.setRequestMethod("POST");
        addConnection.setRequestProperty("Content-Type", "application/json");
        addConnection.setDoOutput(true);

        // Send request body
        String jsonBody = "{\"name\":\"Original Name\"}";
        try (OutputStream os = addConnection.getOutputStream()) {
            byte[] input = jsonBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Verify add response
        int addResponseCode = addConnection.getResponseCode();
        assertEquals(201, addResponseCode);

        // Get all users to find the ID of the added user
        URL getAllUrl = new URL(BASE_URL + "/users");
        HttpURLConnection getAllConnection = (HttpURLConnection) getAllUrl.openConnection();
        getAllConnection.setRequestMethod("GET");

        // Get response
        String getAllResponseBody = getResponseBody(getAllConnection);

        // Extract the ID from the response
        int startIndex = getAllResponseBody.indexOf("\"id\":");
        int endIndex = getAllResponseBody.indexOf(",", startIndex);
        String idStr = getAllResponseBody.substring(startIndex + 5, endIndex).trim();
        int userId = Integer.parseInt(idStr);

        // Update the user
        URL updateUrl = new URL(BASE_URL + "/users/" + userId);
        HttpURLConnection updateConnection = (HttpURLConnection) updateUrl.openConnection();
        updateConnection.setRequestMethod("PUT");
        updateConnection.setRequestProperty("Content-Type", "application/json");
        updateConnection.setDoOutput(true);

        // Send request body
        String updateJsonBody = "{\"name\":\"Updated Name\"}";
        try (OutputStream os = updateConnection.getOutputStream()) {
            byte[] input = updateJsonBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Verify update response
        int updateResponseCode = updateConnection.getResponseCode();
        assertEquals(204, updateResponseCode);

        // Get the user to verify the update
        URL getUserUrl = new URL(BASE_URL + "/users/" + userId);
        HttpURLConnection getUserConnection = (HttpURLConnection) getUserUrl.openConnection();
        getUserConnection.setRequestMethod("GET");

        // Get response
        int getUserResponseCode = getUserConnection.getResponseCode();
        String getUserResponseBody = getResponseBody(getUserConnection);

        // Verify response
        assertEquals(200, getUserResponseCode);
        assertTrue(getUserResponseBody.contains("Updated Name"));
    }

    @Test
    public void testDeleteUser() throws IOException {
        // First, add a user
        URL addUrl = new URL(BASE_URL + "/users");
        HttpURLConnection addConnection = (HttpURLConnection) addUrl.openConnection();
        addConnection.setRequestMethod("POST");
        addConnection.setRequestProperty("Content-Type", "application/json");
        addConnection.setDoOutput(true);

        // Send request body
        String jsonBody = "{\"name\":\"User to Delete\"}";
        try (OutputStream os = addConnection.getOutputStream()) {
            byte[] input = jsonBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Verify add response
        int addResponseCode = addConnection.getResponseCode();
        assertEquals(201, addResponseCode);

        // Get all users to find the ID of the added user
        URL getAllUrl = new URL(BASE_URL + "/users");
        HttpURLConnection getAllConnection = (HttpURLConnection) getAllUrl.openConnection();
        getAllConnection.setRequestMethod("GET");

        // Get response
        String getAllResponseBody = getResponseBody(getAllConnection);

        // Extract the ID from the response
        int startIndex = getAllResponseBody.indexOf("\"id\":");
        int endIndex = getAllResponseBody.indexOf(",", startIndex);
        String idStr = getAllResponseBody.substring(startIndex + 5, endIndex).trim();
        int userId = Integer.parseInt(idStr);

        // Delete the user
        URL deleteUrl = new URL(BASE_URL + "/users/" + userId);
        HttpURLConnection deleteConnection = (HttpURLConnection) deleteUrl.openConnection();
        deleteConnection.setRequestMethod("DELETE");

        // Verify delete response
        int deleteResponseCode = deleteConnection.getResponseCode();
        assertEquals(204, deleteResponseCode);

        // Try to get the deleted user
        URL getUserUrl = new URL(BASE_URL + "/users/" + userId);
        HttpURLConnection getUserConnection = (HttpURLConnection) getUserUrl.openConnection();
        getUserConnection.setRequestMethod("GET");

        // Verify response
        int getUserResponseCode = getUserConnection.getResponseCode();
        assertEquals(404, getUserResponseCode);
    }

    @Test
    public void testGetUsersPaginated() throws IOException {
        // Add multiple users
        for (int i = 0; i < 5; i++) {
            URL addUrl = new URL(BASE_URL + "/users");
            HttpURLConnection addConnection = (HttpURLConnection) addUrl.openConnection();
            addConnection.setRequestMethod("POST");
            addConnection.setRequestProperty("Content-Type", "application/json");
            addConnection.setDoOutput(true);

            // Send request body
            String jsonBody = "{\"name\":\"Paginated User " + i + "\"}";
            try (OutputStream os = addConnection.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Verify add response
            int addResponseCode = addConnection.getResponseCode();
            assertEquals(201, addResponseCode);
        }

        // Get paginated users
        URL paginatedUrl = new URL(BASE_URL + "/users/paginated?page=1&size=3");
        HttpURLConnection paginatedConnection = (HttpURLConnection) paginatedUrl.openConnection();
        paginatedConnection.setRequestMethod("GET");

        // Get response
        int paginatedResponseCode = paginatedConnection.getResponseCode();
        String paginatedResponseBody = getResponseBody(paginatedConnection);

        // Verify response
        assertEquals(200, paginatedResponseCode);

        // The response should be a JSON array with at most 3 users
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
