package edu.gcc.hallmonitor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public record Semester(String semester) implements Filter {

    @Override
    public boolean filter(Course course) {
        return course.semester().equals(semester);
    }

    @Override
    public JsonNode toJSON() {
        ObjectNode root = Main.MAPPER.createObjectNode();
        root.put("type", "semester");
        root.put("value", semester);
        return root;
    }

    private static Filter deserialize(JsonNode value) {
        return new Semester(value.asText());
    }

    private static Set<String> possibleValues(List<Course> courses) {
        return courses.stream()
                .map(Course::semester)
                .collect(Collectors.toSet());
    }

    public static void init() {
        Filter.registerFilterType("semester", Semester::deserialize, Semester::possibleValues);
    }
}

