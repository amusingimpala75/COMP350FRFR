package edu.gcc.hallmonitor;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Schedule {

    private List<Course> courses;
    private static final String SAVED_SCHEDULE = "src/main/java/saved_schedules/saved-schedule.json";

    public Schedule(List<Course> courses) {
        this.courses = courses;
    }
    public Schedule() {

    }

    public static Schedule loadSchedule() throws IOException {
        return new Schedule(Search.loadData(SAVED_SCHEDULE));
    }

    public void saveSchedule() throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, List<Course>> jsonObject = Map.of("classes", courses);
        objectMapper.writeValue(new File(SAVED_SCHEDULE), jsonObject);
    }

    public void addCourse(Course course) {

    }

    public Course removeCourse(Course course) {
        return null;
    }

    public List<Course> getCourses() {
        // TODO: Turn into deep-copy of courses so that mutable object parameters can't be changed
        return courses;
    }


}
