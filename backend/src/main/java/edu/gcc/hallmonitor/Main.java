package edu.gcc.hallmonitor;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.javalin.Javalin;

public class Main {
    private static Schedule currentSchedule;
    public static final ObjectMapper MAPPER;
    static {
        MAPPER = new ObjectMapper();
        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static void main(String[] args) {
        run(7070);
        currentSchedule = new Schedule();
        Search.loadCourses();
    }

    public static void run(int port) {
       Javalin app = Javalin.create(cfg -> { cfg.staticFiles.add("/public"); })
               .start(port);

       SearchController.registerRoutes(app);
       ScheduleController.registerRoutes(app);
    }

    public static Schedule getCurrentSchedule(){
        return currentSchedule;
    }

}
