package edu.gcc.hallmonitor;

import java.util.HashSet;

import com.fasterxml.jackson.databind.JsonNode;

public record HideConflicting() implements Filter {

    // Just to indicate that we are filtering by schedule.
    // We will have to do the calculation elsewhere.
    @Override
    public boolean filter(Course c) {
        return true;
    }

    @Override
    public JsonNode toJSON() {
        var root = Main.MAPPER.createObjectNode();
        root.put("type", "conflicts");
        // empty object
        root.putObject("value");
        return root;
    }

    public static void init() {
        Filter.registerFilterType("conflicts", ing -> new HideConflicting(), ing -> new HashSet<String>());
    }

}
