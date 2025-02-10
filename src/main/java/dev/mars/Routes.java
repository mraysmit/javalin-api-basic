package dev.mars;

import io.javalin.Javalin;

public class Routes {

    public static void configure(Javalin app) {
        app.get("/", RouteHandlers::handleRoot);
        app.get("/hello/{name}", RouteHandlers::handleHello);
        app.get("/query", RouteHandlers::handleQueryParams);
        // Add more routes here
    }
}