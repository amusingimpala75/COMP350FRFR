package edu.gcc.hallmonitor;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

public record HideConflicting(int userId, int scheduleId) implements Filter {

    @Override
    public boolean filter(Course c) {
        try {
            var schedule = ScheduleCache.getSchedule(this);
            return schedule.inSchedule(c) ||
                   schedule.checkForOverlap(c)
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

    private static class ScheduleCache {
        private static Map<HideConflicting, Entry> cache = new HashMap<>();

        public static Schedule getSchedule(HideConflicting filter) throws JsonProcessingException, SQLException {
            if (cache.containsKey(filter) && (cache.get(filter).fetched + 1000) > System.currentTimeMillis()) {
                return cache.get(filter).value;
            }
            var s = Schedule.loadSchedule(filter.userId, filter.scheduleId);
            cache.put(filter, new Entry(System.currentTimeMillis(), s));
            return s;
        }

        private static record Entry(long fetched, Schedule value) { }
    }
}
