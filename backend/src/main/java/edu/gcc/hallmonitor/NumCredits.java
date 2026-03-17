package edu.gcc.hallmonitor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public record NumCredits(int numCredits) implements Filter {

    @Override
    public boolean filter(Course course) {
        return course.credits() == numCredits;
    }
    @Override
    public JsonNode toJSON() {
        ObjectNode root = Main.MAPPER.createObjectNode();
        root.put("type", "credits");
        root.put("value", numCredits);
        return root;
    }

    private static Filter deserialize(JsonNode value) {
        return new NumCredits(value.asInt());
    }

    private static Set<String> possibleValues(List<Course> courses) {
        return courses.stream()
                .map(Course::credits)
                .map(i -> Integer.toString(i))
                .collect(Collectors.toSet());
    }

    public static void init() {
        Filter.registerFilterType("credits", NumCredits::deserialize, NumCredits::possibleValues);
    }
}
