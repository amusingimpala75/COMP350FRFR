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
    void checkForOverlap_noOverlap() {
        Schedule schedule = new Schedule(List.of(
            new Course(
                1, "Course1", List.of("Prof"), "COMP", 101, 'A', "Room", 3, "2023_Fall",
                List.of(new CourseTime("M", LocalTime.of(10, 0), LocalTime.of(11, 0))),
                false, false, 0, 30
            )
        ));

        Course newCourse = new Course(
            2, "Course2", List.of("Prof"), "COMP", 102, 'A', "Room", 3, "2023_Fall",
            List.of(new CourseTime("M", LocalTime.of(11, 0), LocalTime.of(12, 0))),
            false, false, 0, 30
        );

        assertEquals("", schedule.checkForOverlap(newCourse));
    }

    @Test
    void checkForOverlap_overlapSameDay() {
        Schedule schedule = new Schedule(List.of(
            new Course(
                1, "Course1", List.of("Prof"), "COMP", 101, 'A', "Room", 3, "2023_Fall",
                List.of(new CourseTime("M", LocalTime.of(10, 0), LocalTime.of(11, 0))),
                false, false, 0, 30
            )
        ));

        Course newCourse = new Course(
            2, "Course2", List.of("Prof"), "COMP", 102, 'A', "Room", 3, "2023_Fall",
            List.of(new CourseTime("M", LocalTime.of(10, 30), LocalTime.of(11, 30))),
            false, false, 0, 30
        );

        assertTrue(schedule.checkForOverlap(newCourse).contains("overlaps"));
    }
    @Test
    void checkForOverlap_differentDays() {
        Schedule schedule = new Schedule(List.of(
            new Course(
                1, "Course1", List.of("Prof"), "COMP", 101, 'A', "Room", 3, "2023_Fall",
                List.of(new CourseTime("M", LocalTime.of(10,0), LocalTime.of(11,0))),
                false,false,0,30
            )
        ));

        Course newCourse = new Course(
            2, "Course2", List.of("Prof"), "COMP", 102, 'A', "Room", 3, "2023_Fall",
            List.of(new CourseTime("T", LocalTime.of(10,30), LocalTime.of(11,30))),
            false,false,0,30
        );

        assertEquals("", schedule.checkForOverlap(newCourse));
    }
    @Test
    void checkForOverlap_touchingTimesNotOverlap() {
        Schedule schedule = new Schedule(List.of(
            new Course(
                1, "Course1", List.of("Prof"), "COMP", 101, 'A', "Room", 3, "2023_Fall",
                List.of(new CourseTime("M", LocalTime.of(10,0), LocalTime.of(11,0))),
                false,false,0,30
            )
        ));

        Course newCourse = new Course(
            2, "Course2", List.of("Prof"), "COMP", 102, 'A', "Room", 3, "2023_Fall",
            List.of(new CourseTime("M", LocalTime.of(11,0), LocalTime.of(12,0))),
            false,false,0,30
        );

        assertEquals("", schedule.checkForOverlap(newCourse));
    }
    @Test
    void checkForOverlap_invalidTimeIgnored() {
        Schedule schedule = new Schedule(List.of(
            new Course(
                1, "Course1", List.of("Prof"), "COMP", 101, 'A', "Room", 3, "2023_Fall",
                List.of(new CourseTime("M", LocalTime.of(10,0), LocalTime.of(11,0))),
                false,false,0,30
            )
        ));

        // end before start → ignored
        Course newCourse = new Course(
            2, "Course2", List.of("Prof"), "COMP", 102, 'A', "Room", 3, "2023_Fall",
            List.of(new CourseTime("M", LocalTime.of(12,0), LocalTime.of(11,0))),
            false,false,0,30
        );

        assertEquals("", schedule.checkForOverlap(newCourse));
    }


    @Test
    void hasDifferentSection_trueWhenDifferentSection() {
        Schedule schedule = new Schedule(List.of(
            new Course(
                1, "Course1", List.of("Prof"), "COMP", 101, 'A', "Room", 3, "2023_Fall",
                List.of(), false,false,0,30
            )
        ));

        Course newCourse = new Course(
            1, "Course1", List.of("Prof"), "COMP", 101, 'B', "Room", 3, "2023_Fall",
            List.of(), false,false,0,30
        );

        assertTrue(schedule.hasDifferentSection(newCourse));
    }

    @Test
    void hasDifferentSection_falseSameSection() {
        Schedule schedule = new Schedule(List.of(
            new Course(
                1, "Course1", List.of("Prof"), "COMP", 101, 'A', "Room", 3, "2023_Fall",
                List.of(), false,false,0,30
            )
        ));

        Course newCourse = new Course(
            1, "Course1", List.of("Prof"), "COMP", 101, 'A', "Room", 3, "2023_Fall",
            List.of(), false,false,0,30
        );

        assertFalse(schedule.hasDifferentSection(newCourse));
    }

    @Test
    void hasDifferentSection_falseDifferentCourse() {
        Schedule schedule = new Schedule(List.of(
            new Course(
                1, "Course1", List.of("Prof"), "COMP", 101, 'A', "Room", 3, "2023_Fall",
                List.of(), false,false,0,30
            )
        ));

        Course newCourse = new Course(
            2, "Course2", List.of("Prof"), "COMP", 102, 'A', "Room", 3, "2023_Fall",
            List.of(), false,false,0,30
        );

        assertFalse(schedule.hasDifferentSection(newCourse));
    }


}
