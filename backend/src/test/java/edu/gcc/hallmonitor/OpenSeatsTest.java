package edu.gcc.hallmonitor;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OpenSeatsTest {
    @Test
    public void shouldAllowOpenClasses() {
        var open = emptyWithOpen(true);
        var filter = new OpenSeats();
        assertTrue(filter.filter(open));
    }

    @Test
    public void shouldNotAllowClosedClasses() {
        var closed = emptyWithOpen(false);
        var filter = new OpenSeats();
        assertFalse(filter.filter(closed));
    }

    private static Course emptyWithOpen(boolean open) {
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
            List.of(new CourseTime(
                "M",
                LocalTime.of(0, 0),
                LocalTime.of(0, 0)
            )),
            false,
            open,
            0,
            0
        );
    }
}
