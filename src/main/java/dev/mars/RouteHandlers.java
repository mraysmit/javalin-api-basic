package dev.mars;

import io.javalin.http.Context;

public class RouteHandlers {

    public static void handleRoot(Context ctx) {
        ctx.result("Hello, World!");
    }

    public static void handleHello(Context ctx) {
        String name = ctx.pathParam("name");
        ctx.result("Hello, " + name + "!");
    }

    public static void handleQueryParams(Context ctx) {
        String param1 = ctx.queryParam("param1");
        String param2 = ctx.queryParam("param2");
        ctx.result("Query parameters received: param1 = " + param1 + ", param2 = " + param2);
    }

    // Add more handlers here
}