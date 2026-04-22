package edu.gcc.hallmonitor;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DaysTest {
    @Test
    public void testMatchAllDays() {
        Course mwf = emptyOnDays(List.of("M", "W", "F"));
        Course tr = emptyOnDays(List.of("T", "R"));
        Course m = emptyOnDays(List.of("M"));

        Days filter = new Days(List.of("M", "W", "F"));

        assertEquals(false, filter.filter(tr));
        assertEquals(false, filter.filter(m));
        assertEquals(true, filter.filter(mwf));
    }

    @Test
    public void testMatchSingleDay() {
        Course mwf = emptyOnDays(List.of("M", "W", "F"));
        Course tr = emptyOnDays(List.of("T", "R"));
        Course m = emptyOnDays(List.of("M"));

        Days filter = new Days(List.of("M"));

        assertEquals(false, filter.filter(tr));
        assertEquals(true, filter.filter(m));
        assertEquals(false, filter.filter(mwf));
    }

    public Course emptyOnDays(List<String> days) {
        return new Course(
                1,
            "",
            List.of(""),
            "",
            0,
            ' ',
            "",
            0,
            "",
            days.stream()
                    .map(day -> new CourseTime(day, LocalTime.of(0, 0), LocalTime.of(0, 0)))
                    .collect(Collectors.toList()),
            false,
            false,
            0,
            0
        );
    }
}
