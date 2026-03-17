package edu.gcc.hallmonitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public record Days(List<String> days) implements Filter {

    @Override
    public boolean filter(Course course) {
        // If a multi day match, keep only the entries that
        // meet on at least off of the days selected
        if (days.size() > 1) {
            List<String> courseDays = course.times()
                    .stream()
                    .map(CourseTime::day)
                    .collect(Collectors.toList());
            return days.stream()
                    .allMatch(courseDays::contains);
        } else {
            // If a single day match, keep only the classes that
            // meet exactly only on that day
            return course.times().size() == 1
                   && course.times().get(0).day().equals(days.get(0));
        }
    }

    @Override
    public JsonNode toJSON() {
        ObjectNode root = Main.MAPPER.createObjectNode();
        root.put("type", "days");
        ArrayNode values = Main.MAPPER.createArrayNode();
        days().stream()
                .forEach(values::add);
        root.set("value", values);

        return root;
    }

    private static Filter deserialize(JsonNode value) {
        List<String> values = new ArrayList<>();
        value.elements().forEachRemaining(v ->
            values.add(v.asText())
        );
        return new Days(values);
    }

    private static Set<String> possibleValues(List<Course> courses) {
        return courses.stream()
                .map(Course::times)
                .flatMap(List::stream)
                .map(CourseTime::day)
                .collect(Collectors.toSet());
    }

    public static void init() {
        Filter.registerFilterType("days", Days::deserialize, Days::possibleValues);
    }
}
