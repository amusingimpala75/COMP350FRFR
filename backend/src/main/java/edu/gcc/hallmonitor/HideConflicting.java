package edu.gcc.hallmonitor;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

public record HideConflicting(int userId, int scheduleId) implements Filter {

    // Just to indicate that we are filtering by schedule.
    // We will have to do the calculation elsewhere.
    @Override
    public boolean filter(Course c) {
        try {
            return Schedule.loadSchedule(userId, scheduleId)
                    .checkForOverlap(c)
                    .equals("");
        } catch (SQLException sqle) {
            return true;
        } catch (JsonProcessingException jpe) {
            return true;
        }
    }

    @Override
    public JsonNode toJSON() {
        var root = Main.MAPPER.createObjectNode();
        root.put("type", "conflicts");
        var value = root.putObject("value");
        value.put("userId", userId);
        value.put("scheduleId", scheduleId);
        return root;
    }

    private static Filter deserialize(JsonNode value) {
        return new HideConflicting(
            value.get("userId").asInt(),
            value.get("scheduleId").asInt()
        );
    }

    public static void init() {
        Filter.registerFilterType("conflicts", HideConflicting::deserialize, ing -> new HashSet<String>());
    }

}
