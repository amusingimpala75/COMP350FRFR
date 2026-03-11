package edu.gcc.hallmonitor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Schedule {

    private List<Course> courses;
    private static final String SAVED_SCHEDULE = "saved-schedule.json";
    private static final String SAVED_SCHEDULES_FOLDER = "src/main/saved_schedules/";

    public Schedule(List<Course> courses) {
        this.courses = courses;
    }
    public Schedule() {

    }

    public static Schedule loadSchedule(String filename) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        JsonNode root = mapper.readTree(new File(SAVED_SCHEDULES_FOLDER + filename));
        JsonNode classesNode = root.get("classes"); // Grab the courses array inside the json

        return new Schedule(mapper.readerForListOf(Course.class).readValue(classesNode));
    }

    public static Schedule loadSchedule() throws IOException {
        return loadSchedule(SAVED_SCHEDULE);
    }


    public void saveSchedule(String filename) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        Map<String, List<Course>> jsonObject = Map.of("classes", courses);
        mapper.writeValue(new File(SAVED_SCHEDULES_FOLDER + filename), jsonObject);
    }

    public void saveSchedule() throws IOException {
        saveSchedule(SAVED_SCHEDULE);
    }

    public void addCourse(Course course) {

    }

    public Course removeCourse(Course course) {
        return null;
    }

    public List<Course> getCourses() {
        List<Course> copyCourses = new ArrayList<>();
        for (Course c : courses) {
            List<CourseTime> timesCopy = new ArrayList<>();


            for (CourseTime time : c.times()) {

                timesCopy.add(new CourseTime(time.day(), time.startTime(), time.endTime()));
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
