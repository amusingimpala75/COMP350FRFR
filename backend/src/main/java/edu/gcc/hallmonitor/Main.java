package edu.gcc.hallmonitor;

import io.javalin.Javalin;


public class Main {
    private static Schedule currentSchedule;

    public static void main(String[] args) {
        run(7070);
        currentSchedule = new Schedule();
        Search.loadCourses();
    }

    public static void run(int port) {
       Javalin app = Javalin.create(cfg -> { cfg.staticFiles.add("public"); })
               .start(port);

        SearchController.registerRoutes(app);
    }

    public static Schedule getCurrentSchedule(){
        return currentSchedule;
    }

}
