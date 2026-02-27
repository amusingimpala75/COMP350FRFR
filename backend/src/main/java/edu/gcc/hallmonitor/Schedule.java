package edu.gcc.hallmonitor;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Schedule {

    private List<Course> courses;

    public Schedule(List<Course> courses) {
        this.courses = courses;
    }
    public Schedule() {

    }

    public static Schedule loadSchedule(String scheduleFilename) throws IOException {
        return new Schedule(Search.loadData(scheduleFilename));
    }

    public void saveSchedule(String path) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, List<Course>> jsonObject = Map.of("classes", courses);
        objectMapper.writeValue(new File(path), jsonObject);
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
