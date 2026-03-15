package edu.gcc.hallmonitor;

import java.time.LocalTime;
import java.util.List;

public record Time(List<LocalTime> times) implements Filter {

    @Override
    public boolean filter(Course course) {
        return course.times()
                .stream()
                .allMatch(ct -> times.stream()
                        .anyMatch(time -> timesContain(time, ct)));
    }

    private static boolean timesContain(LocalTime time, CourseTime ct) {
        return ct.startTime().compareTo(time) <= 0
               && ct.endTime().compareTo(time) <= 0;
    }
}