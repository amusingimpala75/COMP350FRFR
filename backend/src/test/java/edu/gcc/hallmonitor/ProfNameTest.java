package edu.gcc.hallmonitor;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

public class ProfNameTest {
    @Test
    public void testProfNameFilter() {
        Course dickinson = emptyWithProf("Dickinson");
        Course hutchins = emptyWithProf("Hutchins");

        ProfName filter = new ProfName("Dickinson");

        assertEquals(false, filter.filter(hutchins));
        assertEquals(true, filter.filter(dickinson));
    }

    private static Course emptyWithProf(String prof) {
        return new Course(
            "",
            List.of(prof),
            "",
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
