package edu.gcc.hallmonitor;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Schedule {

    private List<Course> fallCourses;
    private List<Course> springCourses;
    private List<Course> summerCourses;
    private List<Course> winterCourses;
    private static final String SAVED_SCHEDULE = "saved-schedule.json";
    private static final String SAVED_SCHEDULES_FOLDER = "schedules/";

    public Schedule(List<Course> courses) {
        fallCourses = new ArrayList<>();
        springCourses = new ArrayList<>();
        summerCourses = new ArrayList<>();
        winterCourses = new ArrayList<>();

        for(Course c : courses){ //Example semester: 2023_Fall
            addCourse(c);
        }
    }
    public Schedule() {
        this(new ArrayList<Course>());
    }

    public boolean inSchedule(Course course){
        if (course.semester().contains("Fall")){
            //add to fall list
            return fallCourses.contains(course);
        }else if(course.semester().contains("Spring")){
            //add to spring list
            return springCourses.contains(course);
        }else if(course.semester().contains("Summer")){
            //add to summer list
            return summerCourses.contains(course);
        }else{
            //add to winter list
            return winterCourses.contains(course);
        }
    }

    public static Schedule loadSchedule(String filename) throws IOException {
        File f = new File(SAVED_SCHEDULES_FOLDER + filename);
        if (!f.exists()) {
            return new Schedule();
        }

        JsonNode root = Main.MAPPER.readTree(new File(SAVED_SCHEDULES_FOLDER + filename));
        JsonNode classesNode = root.get("classes"); // Grab the courses array inside the json

        return new Schedule(Main.MAPPER.readerForListOf(Course.class).readValue(classesNode));
    }

    public static Schedule loadSchedule() throws IOException {
        return loadSchedule(SAVED_SCHEDULE);
    }

    private List<Course> allCourses(){
        //condense all courses to one list
        List<Course> courses = new ArrayList<>();
        courses.addAll(fallCourses);
        courses.addAll(springCourses);
        courses.addAll(winterCourses);
        courses.addAll(summerCourses);
        return courses;
    }

    public void saveSchedule(String filename) throws IOException {
        File savedDir = new File(SAVED_SCHEDULES_FOLDER);
        if (!savedDir.exists()) {
            savedDir.mkdirs();
        }

        Map<String, List<Course>> jsonObject = Map.of("classes", allCourses());
        Main.MAPPER.writeValue(new File(SAVED_SCHEDULES_FOLDER + filename), jsonObject);
    }

    public void saveSchedule() throws IOException {
        saveSchedule(SAVED_SCHEDULE);
    }

    public void addCourse(Course course) {
        if (course.semester().contains("Fall")){
            //add to fall list
            fallCourses.add(course);
        }else if(course.semester().contains("Spring")){
            //add to spring list
            springCourses.add(course);
        }else if(course.semester().contains("Summer")){
            //add to summer list
            summerCourses.add(course);
        }else{
            //add to winter list
            winterCourses.add(course);
        }
    }

    public boolean removeCourse(Course course) {
        if (course.semester().contains("Fall")){
            //add to fall list
            return fallCourses.remove(course);
        }else if(course.semester().contains("Spring")){
            //add to spring list
            return springCourses.remove(course);
        }else if(course.semester().contains("Summer")){
            return summerCourses.remove(course);
        }else{
            return winterCourses.remove(course);
        }
    }

    //TODO: add a checkForOverlap method, and make sure to check only in the same term
    public String checkForOverlap(Course c){

        return "stub";
    }

    public List<Course> getCourses() {
        List<Course> copyCourses = new ArrayList<>();
        for (Course c : allCourses()) {
            //if the course can't be found, don't cause a server error
            if(c == null) continue;
            List<CourseTime> timesCopy = new ArrayList<>();

            if(c.times() != null) {
                for (CourseTime time : c.times()) {

                    timesCopy.add(new CourseTime(time.day(), time.startTime(), time.endTime()));
                }
            }

            Course copyCourse = new Course(
                    c.name(),
                    new ArrayList<>(c.professor()),
                    c.department(),
                    c.code(),
                    c.section(),
                    c.location(),
                    c.credits(),
                    c.semester(),
                    timesCopy,
                    c.isLab(),
                    c.isOpen(),
                    c.numOpenSeats(),
                    c.totalSeats()
            );

            copyCourses.add(copyCourse);
        }

        return copyCourses;
    }


}
