package edu.gcc.hallmonitor;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.io.File;
import java.util.Currency;

public class Schedule {

    private ArrayList<Course> courses;



    public Schedule(ArrayList<Course> courses) {
        this.courses = new ArrayList<>(courses);
    }

    public Schedule() {
        courses = new ArrayList<>();
    }

    public boolean inSchedule(Course course){
        return courses.contains(course);
    }

    public static Schedule loadSchedule(File f) {
        return null;
    }

    public void saveSchedule() {

    }

    public void addCourse(Course course) {
        courses.add(course);
    }

    public boolean removeCourse(Course course) {
        return courses.remove(course);
    }

    public ArrayList<Course> getCourses() {
        return courses;
    }


}
