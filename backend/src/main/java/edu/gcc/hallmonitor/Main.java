package edu.gcc.hallmonitor;

import io.javalin.Javalin;



public class Main {
    public static void main(String[] args) {
        run(7070);
    }

    public static void run(int port) {

        Javalin.create(cfg -> { cfg.staticFiles.add("public"); })
                .get("/", ctx -> ctx.result("Hello, world"))
                .start(port);
    }

}
