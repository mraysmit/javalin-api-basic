package dev.mars.integration;

import com.google.inject.Guice;
import com.google.inject.Injector;
import dev.mars.Main;
import dev.mars.config.ApplicationProperties;
import dev.mars.di.ApplicationModule;
import io.javalin.Javalin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the enhanced Javalin API.
 */
public class EnhancedApiIntegrationTest {
    
    private static Javalin app;
    private static HttpClient httpClient;
    private static final int TEST_PORT = 8081;
    private static final String BASE_URL = "http://localhost:" + TEST_PORT;

    @BeforeAll
    static void setUp() {
        // Initialize dependency injection for testing
        Injector injector = Guice.createInjector(new ApplicationModule());
        ApplicationProperties properties = injector.getInstance(ApplicationProperties.class);
        
        // Override port for testing
        properties.getServer().setPort(TEST_PORT);
        
        // Start the application (simplified version for testing)
        app = Javalin.create().start(TEST_PORT);
        
        // Initialize HTTP client
        httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }

    @AfterAll
    static void tearDown() {
        if (app != null) {
            app.stop();
        }
    }

    @Test
    void testHealthEndpoint() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/health"))
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("status"));
    }

    @Test
    void testMetricsEndpoint() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/metrics"))
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        assertEquals("text/plain; version=0.0.4; charset=utf-8", response.headers().firstValue("content-type").orElse(""));
    }

    @Test
    void testApiDocumentationEndpoint() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api-docs"))
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("openapi"));
        assertTrue(response.body().contains("3.0.3"));
    }

    @Test
    void testSwaggerUiEndpoint() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/swagger-ui"))
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("swagger-ui"));
        assertEquals("text/html", response.headers().firstValue("content-type").orElse(""));
    }

    @Test
    void testCacheStatsEndpoint() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/cache/stats"))
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("hitCount"));
        assertTrue(response.body().contains("missCount"));
    }

    @Test
    void testVersionedUserEndpoints() throws Exception {
        // Test GET /api/v1/users
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/v1/users"))
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        assertTrue(response.body().startsWith("["));
    }

    @Test
    void testVersionedTradeEndpoints() throws Exception {
        // Test GET /api/v1/trades
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/v1/trades"))
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        assertTrue(response.body().startsWith("["));
    }

    @Test
    void testPaginatedEndpoints() throws Exception {
        // Test paginated users endpoint
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/v1/users/paginated?page=0&size=10"))
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("content"));
        assertTrue(response.body().contains("metadata"));
    }

    @Test
    void testValidationErrors() throws Exception {
        // Test creating user with invalid data
        String invalidUserJson = "{\"name\":\"\",\"email\":\"invalid-email\"}";
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/v1/users"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(invalidUserJson))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("error"));
        assertTrue(response.body().contains("Validation failed"));
    }
}
