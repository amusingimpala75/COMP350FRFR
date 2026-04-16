package edu.gcc.hallmonitor;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DepartmentTest {
    @Test
    public void testDepartmentFilter() {
        Course cs = emptyWithDepartment("CS");
        Course huma = emptyWithDepartment("HUMA");

        Department filter = new Department("HUMA");

        assertEquals(true, filter.filter(huma));
        assertEquals(false, filter.filter(cs));
    }

    private static Course emptyWithDepartment(String department) {
        return new Course(
            "",
            List.of(""),
            department,
            0,
            ' ',
            "",
            0,
            "",
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
