package edu.gcc.hallmonitor;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.javalin.Javalin;

public class Main {

    public static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static void main(String[] args) {
        Filter.init();
        run(7070);
    }

    public static void run(int port) {
        Javalin app = Javalin.create(cfg -> { cfg.staticFiles.add("/public"); })
                .start(port);

        AuthController.registerRoutes(app);
        SearchController.registerRoutes(app);
        ScheduleController.registerRoutes(app);
    }

    public static String readResource(String path) throws IOException {
        try (InputStream is = Main.class.getResourceAsStream(path)) {
            return new String(is.readAllBytes());
        }
    }
}
