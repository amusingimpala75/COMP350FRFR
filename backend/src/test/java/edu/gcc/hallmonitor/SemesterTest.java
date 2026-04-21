package edu.gcc.hallmonitor;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SemesterTest {
    @Test
    public void testSemesterFilter() {
        Course fall = emptyWithSemester("2023_Fall");
        Course spring = emptyWithSemester("2024_Spring");

        Semester filter = new Semester("2023_Fall");

        assertEquals(true, filter.filter(fall));
        assertEquals(false, filter.filter(spring));
    }

    private static Course emptyWithSemester(String semester) {
        return new Course(
            "",
            List.of(""),
            "",
            0,
            ' ',
            "",
            0,
            semester,
            List.of(new CourseTime(
                "M",
                LocalTime.of(0, 0),
                LocalTime.of(0, 0)
            )),
            false,
            false,
            0,
            0
        );
    }
}

