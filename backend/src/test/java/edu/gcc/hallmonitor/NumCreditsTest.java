package edu.gcc.hallmonitor;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NumCreditsTest {
    @Test
    public void testNumCreditsFilter() {
        Course two = emptyWithCredits(2);
        Course four = emptyWithCredits(4);

        NumCredits filter = new NumCredits(4);

        assertEquals(true, filter.filter(four));
        assertEquals(false, filter.filter(two));
    }

    private static Course emptyWithCredits(int credits) {
        return new Course(
                1,
            "",
            List.of(""),
            "",
            0,
            ' ',
            "",
            credits,
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
