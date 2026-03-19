package edu.gcc.hallmonitor;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public record Time(LocalTime start, LocalTime end) implements Filter {

    @Override
    public boolean filter(Course course) {
        return !course.times().isEmpty() &&
               course.times()
                       .stream()
                       .allMatch(ct -> start.isBefore(ct.startTime()) && end.isAfter(ct.endTime()));
    }

    @Override
    public JsonNode toJSON() {
        ObjectNode root = Main.MAPPER.createObjectNode();
        root.put("type", "timeRange");
        ObjectNode value = Main.MAPPER.createObjectNode();
        value.put("start", start.toString());
        value.put("end", end.toString());
        root.set("value", value);

        return root;
    }

    private static Filter deserialize(JsonNode value) {
        String[] startList = value.get("start").asText().split(":");
        LocalTime start = LocalTime.of(Integer.parseInt(startList[0]), Integer.parseInt(startList[1]));
        String[] endList = value.get("end").asText().split(":");
        LocalTime end = LocalTime.of(Integer.parseInt(endList[0]), Integer.parseInt(endList[1]));
        return new Time(start, end);
    }

    private static Set<String> possibleValues(List<Course> courses) {
        Set<String> times = new HashSet<>();
        courses.stream()
                .map(Course::times)
                .flatMap(List::stream)
                .map(time -> List.of(time.startTime(), time.endTime()))
                .flatMap(List::stream)
                .map(time -> time.format(DateTimeFormatter.ofPattern("HH:MM")))
                .forEach(times::add);
        return times;
    }

    public static void init() {
        Filter.registerFilterType("timeRange", Time::deserialize, Time::possibleValues);
    }
}
