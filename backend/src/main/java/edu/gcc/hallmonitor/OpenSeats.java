package edu.gcc.hallmonitor;

import java.util.HashSet;

import com.fasterxml.jackson.databind.JsonNode;

public record OpenSeats() implements Filter {
    @Override
    public boolean filter(Course c) {
        return c.isOpen();
    }

    public JsonNode toJSON() {
        var root = Main.MAPPER.createObjectNode();
        root.put("type", "open");
        // Empty object
        root.putObject("value");
        return root;
    }

    public static void init() {
        Filter.registerFilterType(
            "open",
            ign -> new OpenSeats(),
            ign ->  new HashSet<String>()
        );
    }
}
