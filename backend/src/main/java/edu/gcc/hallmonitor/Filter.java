package edu.gcc.hallmonitor;

import java.util.function.Predicate;


import com.fasterxml.jackson.databind.JsonNode;

public interface Filter extends Predicate<Course> {
    boolean filter(Course c);

    default boolean test(Course c) {
        return filter(c);
    }

    // [TODO] use Reflections and another interface method to automate this
    static Filter fromJSON(JsonNode root) {
        String type = root.get("type").asText();
        JsonNode value = root.get("value");
        switch (type) {
            case "professor": return new ProfName(value.asText());
            case "credits": return new NumCredits(value.asInt());
            // case "time":
            // case "days":
            case "department": return new Department(value.asText());
        }
        throw new IllegalStateException("invalid filter type: " + type);
    }
}
