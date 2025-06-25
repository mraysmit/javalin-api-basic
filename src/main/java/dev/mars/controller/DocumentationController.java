package dev.mars.controller;

import com.google.inject.Inject;
import dev.mars.config.ApplicationProperties;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for API documentation endpoints.
 */
public class DocumentationController {
    private static final Logger logger = LoggerFactory.getLogger(DocumentationController.class);
    
    private final ApplicationProperties properties;

    @Inject
    public DocumentationController(ApplicationProperties properties) {
        this.properties = properties;
    }

    /**
     * Serves the OpenAPI specification.
     */
    public void getOpenApiSpec(Context ctx) {
        try {
            String openApiSpec = generateOpenApiSpec();
            ctx.contentType("application/json").result(openApiSpec);
        } catch (Exception e) {
            logger.error("Error generating OpenAPI spec", e);
            ctx.status(500).json("Error generating API documentation");
        }
    }

    /**
     * Serves the Swagger UI.
     */
    public void getSwaggerUi(Context ctx) {
        try {
            String swaggerHtml = generateSwaggerUiHtml();
            ctx.contentType("text/html").result(swaggerHtml);
        } catch (Exception e) {
            logger.error("Error generating Swagger UI", e);
            ctx.status(500).result("Error loading API documentation");
        }
    }

    private String generateOpenApiSpec() {
        return """
        {
          "openapi": "3.0.3",
          "info": {
            "title": "Enhanced Javalin API",
            "description": "A comprehensive REST API built with Javalin featuring dependency injection, caching, metrics, and validation",
            "version": "%s",
            "contact": {
              "name": "API Support",
              "email": "support@example.com"
            }
          },
          "servers": [
            {
              "url": "http://localhost:%d/api/v1",
              "description": "Development server"
            }
          ],
          "paths": {
            "/users": {
              "get": {
                "summary": "Get all users",
                "tags": ["Users"],
                "responses": {
                  "200": {
                    "description": "List of users",
                    "content": {
                      "application/json": {
                        "schema": {
                          "type": "array",
                          "items": { "$ref": "#/components/schemas/User" }
                        }
                      }
                    }
                  }
                }
              },
              "post": {
                "summary": "Create a new user",
                "tags": ["Users"],
                "requestBody": {
                  "required": true,
                  "content": {
                    "application/json": {
                      "schema": { "$ref": "#/components/schemas/User" }
                    }
                  }
                },
                "responses": {
                  "201": { "description": "User created successfully" },
                  "400": { "description": "Validation error" }
                }
              }
            },
            "/users/{id}": {
              "get": {
                "summary": "Get user by ID",
                "tags": ["Users"],
                "parameters": [
                  {
                    "name": "id",
                    "in": "path",
                    "required": true,
                    "schema": { "type": "integer" }
                  }
                ],
                "responses": {
                  "200": {
                    "description": "User details",
                    "content": {
                      "application/json": {
                        "schema": { "$ref": "#/components/schemas/User" }
                      }
                    }
                  },
                  "404": { "description": "User not found" }
                }
              }
            },
            "/trades": {
              "get": {
                "summary": "Get all trades",
                "tags": ["Trades"],
                "responses": {
                  "200": {
                    "description": "List of trades",
                    "content": {
                      "application/json": {
                        "schema": {
                          "type": "array",
                          "items": { "$ref": "#/components/schemas/Trade" }
                        }
                      }
                    }
                  }
                }
              }
            }
          },
          "components": {
            "schemas": {
              "User": {
                "type": "object",
                "required": ["name"],
                "properties": {
                  "id": { "type": "integer", "readOnly": true },
                  "name": { "type": "string", "minLength": 2, "maxLength": 100 },
                  "email": { "type": "string", "format": "email" }
                }
              },
              "Trade": {
                "type": "object",
                "required": ["symbol", "quantity", "price", "type"],
                "properties": {
                  "id": { "type": "integer", "readOnly": true },
                  "symbol": { "type": "string", "minLength": 1, "maxLength": 10 },
                  "quantity": { "type": "integer", "minimum": 1 },
                  "price": { "type": "number", "minimum": 0 },
                  "type": { "type": "string", "enum": ["BUY", "SELL"] },
                  "status": { "type": "string" },
                  "tradeDate": { "type": "string", "format": "date" },
                  "settlementDate": { "type": "string", "format": "date" },
                  "counterparty": { "type": "string" },
                  "notes": { "type": "string", "maxLength": 500 }
                }
              }
            }
          }
        }
        """.formatted(properties.getApi().getVersion(), properties.getServer().getPort());
    }

    private String generateSwaggerUiHtml() {
        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8">
          <title>Enhanced Javalin API Documentation</title>
          <link rel="stylesheet" type="text/css" href="https://unpkg.com/swagger-ui-dist@4.15.5/swagger-ui.css" />
          <style>
            html { box-sizing: border-box; overflow: -moz-scrollbars-vertical; overflow-y: scroll; }
            *, *:before, *:after { box-sizing: inherit; }
            body { margin:0; background: #fafafa; }
          </style>
        </head>
        <body>
          <div id="swagger-ui"></div>
          <script src="https://unpkg.com/swagger-ui-dist@4.15.5/swagger-ui-bundle.js"></script>
          <script src="https://unpkg.com/swagger-ui-dist@4.15.5/swagger-ui-standalone-preset.js"></script>
          <script>
            window.onload = function() {
              const ui = SwaggerUIBundle({
                url: '/api-docs',
                dom_id: '#swagger-ui',
                deepLinking: true,
                presets: [
                  SwaggerUIBundle.presets.apis,
                  SwaggerUIStandalonePreset
                ],
                plugins: [
                  SwaggerUIBundle.plugins.DownloadUrl
                ],
                layout: "StandaloneLayout"
              });
            };
          </script>
        </body>
        </html>
        """;
    }
}
