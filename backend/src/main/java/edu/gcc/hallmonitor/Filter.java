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
        return DESERIALIZERS.get(type).apply(value);
    }

    static Set<String> possibleValues(String type, List<Course> courses) {
        return VALUE_FETCHERS.get(type).apply(courses);
    }

    Map<String, Function<JsonNode, Filter>> DESERIALIZERS = new HashMap<>();
    Map<String, Function<List<Course>, Set<String>>> VALUE_FETCHERS = new HashMap<>();

    static void registerFilterType(
        String name,
        Function<JsonNode, Filter> deserialize,
        Function<List<Course>, Set<String>> valueFetcher
    ) {
        DESERIALIZERS.put(name, deserialize);
        VALUE_FETCHERS.put(name, valueFetcher);
    }

    static void init() {
        Days.init();
        Department.init();
        Semester.init();
        NumCredits.init();
        ProfName.init();
        Time.init();
    }
}
