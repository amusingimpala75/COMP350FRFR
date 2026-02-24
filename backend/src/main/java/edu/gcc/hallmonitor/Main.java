package edu.gcc.hallmonitor;

import io.javalin.Javalin;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        run(7070);
    }

    public static void run(int port) {
        try {
            List<Course> courses = loadData("courses.json");
        } catch (FileNotFoundException fnfe) {
            System.err.println("File not found: " + fnfe.getMessage());
        } catch (IOException ioe) {
            System.err.println("IO Exception occurred: " + ioe.getMessage());
        }

        Javalin.create(cfg -> { cfg.staticFiles.add("public"); })
                .get("/", ctx -> ctx.result("Hello, world"))
                .start(port);
    }

    public static List<Course> loadData(String coursesFilename) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        URL jsonURL = Main.class.getResource(String.format("/%s", coursesFilename));
        if (jsonURL == null) {
            throw new FileNotFoundException(String.format("Could not find '%s' in resources directory", coursesFilename));
        }

        JsonNode root = mapper.readTree(jsonURL);
        JsonNode classesNode = root.get("classes"); // Grab the courses array inside the json

        return mapper.readerForListOf(Course.class).readValue(classesNode);
    }
}
