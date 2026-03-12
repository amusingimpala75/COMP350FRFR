package edu.gcc.hallmonitor;

import java.util.List;
import java.util.stream.Collectors;

public class Days extends Filter {

    private List<String> days;

    public Days(List<String> days) {
        this.days = days;
    }

    @Override
    public boolean filter(Course course) {
        // If a multi day match, keep only the entries that
        // meet on at least off of the days selected
        if (days.size() > 1) {
            List<String> courseDays = course.times()
                    .stream()
                    .map(CourseTime::day)
                    .collect(Collectors.toList());
            return days.stream()
                    .allMatch(courseDays::contains);
        } else {
            // If a single day match, keep only the classes that
            // meet exactly only on that day
            return course.times().size() == 1
                   && course.times().get(0).day().equals(days.get(0));
        }
    }
}