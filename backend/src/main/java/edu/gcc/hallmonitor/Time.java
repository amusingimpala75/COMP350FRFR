package edu.gcc.hallmonitor;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public record Time(List<LocalTime> times) implements Filter {

    @Override
    public boolean filter(Course course) {
        return course.times()
                .stream()
                .allMatch(ct -> times.stream()
                        .anyMatch(time -> timesContain(time, ct)));
    }

    private static boolean timesContain(LocalTime time, CourseTime ct) {
        return ct.startTime().compareTo(time) <= 0
               && ct.endTime().compareTo(time) <= 0;
    }

    @Override
    public JsonNode toJSON() {
        ObjectNode root = Main.MAPPER.createObjectNode();
        root.put("type", "days");
        ArrayNode values = Main.MAPPER.createArrayNode();
        times().stream()
                .map(LocalTime::toString)
                .forEach(values::add);
        root.set("value", values);

        return root;
    }

    private static Filter deserialize(JsonNode value) {
        // [TODO]
        throw new UnsupportedOperationException("unimplemented");
    }

    private static Set<String> possibleValues(List<Course> courses) {
        // [TODO]
        throw new UnsupportedOperationException("unimplemented");
    }

    public static void init() {
        Filter.registerFilterType("time", Time::deserialize, Time::possibleValues);
    }
}
