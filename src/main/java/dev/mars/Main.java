package dev.mars;

import io.javalin.Javalin;


public class Main {
    public static void main(String[] args) {

        var app = Javalin.create(/*config*/)
                .start(7070);

        Routes.configure(app);
    }
}