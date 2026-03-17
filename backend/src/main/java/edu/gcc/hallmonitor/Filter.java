package edu.gcc.hallmonitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import com.fasterxml.jackson.databind.JsonNode;

public interface Filter extends Predicate<Course> {
    boolean filter(Course c);
    JsonNode toJSON();

    default boolean test(Course c) {
        return filter(c);
    }

    static Filter fromJSON(JsonNode root) {
        String type = root.get("type").asText();
        JsonNode value = root.get("value");
        return deserializers.get(type).apply(value);
    }

    static Set<String> possibleValues(String type, List<Course> courses) {
        return valueFetchers.get(type).apply(courses);
    }

    static Map<String, Function<JsonNode, Filter>> deserializers = new HashMap<>();
    static Map<String, Function<List<Course>, Set<String>>> valueFetchers = new HashMap<>();

    static void registerFilterType(
        String name,
        Function<JsonNode, Filter> deserialize,
        Function<List<Course>, Set<String>> valueFetcher
    ) {
        deserializers.put(name, deserialize);
        valueFetchers.put(name, valueFetcher);
    }

    static void init() {
        Days.init();
        Department.init();
        NumCredits.init();
        ProfName.init();
        Time.init();
    }
}
