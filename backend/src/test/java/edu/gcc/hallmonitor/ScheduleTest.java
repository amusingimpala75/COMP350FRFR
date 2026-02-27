package edu.gcc.hallmonitor;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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
                assertEquals(expectedCourse.times().get(j).get("day"), actualCourse.times().get(j).get("day"));
                assertEquals(expectedCourse.times().get(j).get("end_time"), actualCourse.times().get(j).get("end_time"));
                assertEquals(expectedCourse.times().get(j).get("start_time"), actualCourse.times().get(j).get("start_time"));
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
                                Map.of(
                                        "day", "M",
                                        "end_time", "12:50:00",
                                        "start_time", "12:00:00"
                                ),
                                Map.of(
                                        "day", "W",
                                        "end_time", "12:50:00",
                                        "start_time", "12:00:00"
                                ),
                                Map.of(
                                        "day", "F",
                                        "end_time", "12:50:00",
                                        "start_time", "12:00:00"
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
                                Map.of(
                                        "day", "T",
                                        "end_time", "12:15:00",
                                        "start_time", "11:00:00"
                                ),
                                Map.of(
                                        "day", "R",
                                        "end_time", "12:15:00",
                                        "start_time", "11:00:00"
                                )
                        ),
                        false,
                        false,
                        0,
                        32
                )
        ));
        try {
            expectedSchedule.saveSchedule();
        } catch (IOException ioe) {
            fail("Failed to save schedule");
        }

        Schedule actualSchedule = null;
        try {
            actualSchedule = Schedule.loadSchedule();
        } catch (IOException ioe) {
            fail("Unable to load schedule.json");
        }

        assertSchedulesEqual(expectedSchedule, actualSchedule);
    }
}