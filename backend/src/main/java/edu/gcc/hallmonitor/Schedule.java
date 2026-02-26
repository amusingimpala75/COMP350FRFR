package edu.gcc.hallmonitor;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Schedule {

    private List<Course> courses;

    public Schedule(List<Course> courses) {

    }
    public Schedule() {

    }

    public static Schedule loadSchedule(String scheduleFilename) throws IOException {
        return new Schedule(Search.loadData(scheduleFilename));
    }

    public void saveSchedule(String scheduleName) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(new File(scheduleName), courses);
    }

    public void addCourse(Course course) {

    }

    public Course removeCourse(Course course) {
        return null;
    }

    public List<Course> getCourses() {
        return null;
    }


}
