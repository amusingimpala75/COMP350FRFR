package edu.gcc.hallmonitor;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

public class TimeTest {
    @Test
    public void testMatchTime() {
        Course c10 = emptyAtTimes(List.of(LocalTime.of(10, 0)));
        Course c2 = emptyAtTimes(List.of(LocalTime.of(14, 0)));

        Time filter = new Time(LocalTime.of(9, 0), LocalTime.of(13, 0));

        assertEquals(false, filter.filter(c2));
        assertEquals(true, filter.filter(c10));
    }

    private static Course emptyAtTimes(List<LocalTime> times) {
        return new Course(
            "",
            List.of(""),
            "",
            0,
            ' ',
            "",
            0,
            "",
            times.stream()
                    .map(time -> new CourseTime("M", time, time))
                    .collect(Collectors.toList()),
            false,
            false,
            0,
            0
        );
    }
}
