package edu.gcc.hallmonitor;

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

    public void saveSchedule() {

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
