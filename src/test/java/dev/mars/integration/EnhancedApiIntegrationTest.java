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
        try {
            // Initialize dependency injection for testing
            Injector injector = Guice.createInjector(new ApplicationModule());
            ApplicationProperties properties = injector.getInstance(ApplicationProperties.class);

            // Override port for testing
            properties.getServer().setPort(TEST_PORT);

            // Get controllers from injector
            var baseController = injector.getInstance(dev.mars.controller.BaseController.class);
            var userController = injector.getInstance(dev.mars.controller.UserController.class);
            var tradeController = injector.getInstance(dev.mars.controller.TradeController.class);
            var metricsController = injector.getInstance(dev.mars.controller.MetricsController.class);
            var documentationController = injector.getInstance(dev.mars.controller.DocumentationController.class);

            // Start the application with proper configuration
            app = Javalin.create(config -> {
                config.bundledPlugins.enableCors(cors -> {
                    cors.addRule(it -> {
                        it.allowHost("http://localhost:3000", "http://localhost:" + TEST_PORT);
                        it.allowCredentials = false;
                    });
                });
                config.bundledPlugins.enableDevLogging();
            }).start(TEST_PORT);

            // Register all routes like in the main application
            app.get("/health", metricsController::getHealth);
            app.get("/metrics", metricsController::getMetrics);
            app.get("/cache/stats", metricsController::getCacheStats);
            app.get("/api-docs", documentationController::getOpenApiSpec);
            app.get("/swagger-ui", documentationController::getSwaggerUi);

            // Register versioned API routes
            dev.mars.routes.v1.UserRoutesV1.register(app, userController);
            dev.mars.routes.v1.TradeRoutesV1.register(app, tradeController);

            // Initialize HTTP client
            httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

            // Wait a bit for the server to fully start
            Thread.sleep(1000);

        } catch (Exception e) {
            throw new RuntimeException("Failed to set up integration test", e);
        }
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
