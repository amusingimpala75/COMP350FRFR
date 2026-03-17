package edu.gcc.hallmonitor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public record ProfName(String profName) implements Filter {

    @Override
    public boolean filter(Course course) {
        return course.professor().contains(profName);
    }

    @Override
    public JsonNode toJSON() {
        ObjectNode root = Main.MAPPER.createObjectNode();
        root.put("type", "professor");
        root.put("value", profName);
        return root;
    }

    private static Filter deserialize(JsonNode value) {
        return new ProfName(value.asText());
    }

    private static Set<String> possibleValue(List<Course> courses) {
        return courses.stream()
                .map(Course::professor)
                .flatMap(List::stream)
                .collect(Collectors.toSet());

    }

    public static void init() {
        Filter.registerFilterType("professor", ProfName::deserialize, ProfName::possibleValue);
    }
}
