package edu.gcc.hallmonitor;

import java.io.IOException;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ScheduleTest {

    void assertSchedulesEqual(Schedule expected, Schedule actual) {
        List<Course> expectedCourses = expected.getCourses();
        List<Course> actualCourses = actual.getCourses();

        assertEquals(expectedCourses.size(), actualCourses.size());
        for (int i = 0; i < actualCourses.size(); i++) {
            Course expectedCourse = expectedCourses.get(i);
            Course actualCourse = actualCourses.get(i);

            assertEquals(expectedCourse.name(), actualCourse.name());
            assertEquals(expectedCourse.professor().size(), actualCourse.professor().size());
            for (int j = 0; j < actualCourse.professor().size(); j++) {
                assertEquals(expectedCourse.professor().get(j), actualCourse.professor().get(j));
            }
            assertEquals(expectedCourse.department(), actualCourse.department());
            assertEquals(expectedCourse.code(), actualCourse.code());
            assertEquals(expectedCourse.section(), actualCourse.section());
            assertEquals(expectedCourse.location(), actualCourse.location());
            assertEquals(expectedCourse.credits(), actualCourse.credits());
            assertEquals(expectedCourse.semester(), actualCourse.semester());
            assertEquals(expectedCourse.times().size(), actualCourse.times().size());
            for (int j = 0; j < actualCourse.times().size(); j++) {
                assertEquals(expectedCourse.times().get(j), actualCourse.times().get(j));
            }
            assertEquals(expectedCourse.isLab(), actualCourse.isLab());
            assertEquals(expectedCourse.isOpen(), actualCourse.isOpen());
            assertEquals(expectedCourse.numOpenSeats(), actualCourse.numOpenSeats());
            assertEquals(expectedCourse.totalSeats(), actualCourse.totalSeats());
        }
    }

    @Test
    void loadSaveSchedule() {
        Schedule expectedSchedule = new Schedule(List.of(
                new Course(
                        "COMP PROGRAMMING I",
                        List.of("Wolfe, Britton D."),
                        "COMP",
                        141,
                        'A',
                        "Science Technology Engineering",
                        3,
                        "2023_Fall",
                        List.of(
                                new CourseTime(
                                        "M",
                                        LocalTime.of(12, 0, 0),
                                        LocalTime.of(12, 50, 0)
                                ),
                                new CourseTime(
                                        "W",
                                        LocalTime.of(12, 0, 0),
                                        LocalTime.of(12, 50, 0)
                                ),
                                new CourseTime(
                                        "F",
                                        LocalTime.of(12, 0, 0),
                                        LocalTime.of(12, 50, 0)
                                )
                        ),
                        false,
                        false,
                        0,
                        32
                ),
                new Course(
                        "INTRO TO COMPUTER SCIENCE",
                        List.of("Dickinson, Brian C"),
                        "COMP",
                        155,
                        'A',
                        "STEM 326",
                        3,
                        "2023_Fall",
                        List.of(
                                new CourseTime(
                                        "T",
                                        LocalTime.of(11, 0, 0),
                                        LocalTime.of(12, 15, 0)
                                ),
                                new CourseTime(
                                        "R",
                                        LocalTime.of(11, 0, 0),
                                        LocalTime.of(12, 15, 0)
                                )
                        ),
                        false,
                        false,
                        0,
                        32
                )
        ));
        try {
            expectedSchedule.saveSchedule("test.json");
        } catch (IOException ioe) {
            fail("Failed to save schedule");
        }

        Schedule actualSchedule = null;
        try {
            actualSchedule = Schedule.loadSchedule("test.json");
        } catch (IOException ioe) {
            fail("Unable to load schedule.json");
        }

        assertSchedulesEqual(expectedSchedule, actualSchedule);
    }
}
