package edu.gcc.hallmonitor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public record Department(String department) implements Filter {

    @Override
    public boolean filter(Course course) {
        return course.department().equals(department);
    }

    @Override
    public JsonNode toJSON() {
        ObjectNode root = Main.MAPPER.createObjectNode();
        root.put("type", "department");
        root.put("value", department);
        return root;
    }

    private static Filter deserialize(JsonNode value) {
        return new Department(value.asText());
    }

    private static Set<String> possibleValues(List<Course> courses) {
        return courses.stream()
                .map(Course::department)
                // Don't include ZLOAD "courses"
                .filter(s -> !s.equals("ZLOAD"))
                .collect(Collectors.toSet());
    }

    public static void init() {
        Filter.registerFilterType("department", Department::deserialize, Department::possibleValues);
    }
}
