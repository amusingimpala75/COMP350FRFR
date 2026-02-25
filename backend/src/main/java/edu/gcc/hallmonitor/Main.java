package edu.gcc.hallmonitor;

import io.javalin.http.staticfiles.Location;
import io.javalin.Javalin;


public class Main {
    public static void main(String[] args) {
        run(7070);
    }

    public static void run(int port) {

        //old code that didn't connect with the index.html file
//        Javalin app = Javalin.create(cfg -> { cfg.staticFiles.add("public"); })
//                .get("/", ctx -> ctx.result("Hello, World"))
//                .start(port);

//        Javalin app = Javalin.create(config -> {
//            config.staticFiles.add(staticFiles -> {
//                staticFiles.hostedPath = "/";
//                staticFiles.directory = "../frontend/public";
//                staticFiles.location = Location.EXTERNAL;
//            });
//        }).start(port);

       Javalin app = Javalin.create(cfg -> {
           cfg.staticFiles.add("/dist", Location.CLASSPATH);
       }).start(port);

        SearchController.registerRoutes(app);
    }

}
