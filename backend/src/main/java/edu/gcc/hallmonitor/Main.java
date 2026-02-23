package edu.gcc.hallmonitor;

import io.javalin.Javalin;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        run(7070);
    }

    public static void run(int port) {
        try {
            List<Course> courses = loadData("src/main/resources/courses.json");
        } catch (IOException ioe) {
            System.err.println("IO Exception occured: " + ioe.getMessage());
        }

        Javalin.create(cfg -> { cfg.staticFiles.add("public"); })
                .get("/", ctx -> ctx.result("Hello, world"))
                .start(port);
    }

    public static List<Course> loadData(String coursesFilename) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode root = mapper.readTree(new File(coursesFilename));
        JsonNode classesNode = root.get("classes"); // Grab the courses array inside the json

        return mapper.readerForListOf(Course.class).readValue(classesNode);
    }
}
