package edu.gcc.hallmonitor;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Schedule {

    private List<Course> courses;
    private static final String SAVED_SCHEDULE = "saved-schedule.json";
    private static final String SAVED_SCHEDULES_FOLDER = "schedules/";

    public Schedule(List<Course> courses) {
        this.courses = courses;
    }
    public Schedule() {
        this(new ArrayList<Course>());
    }

    public boolean inSchedule(Course course) {
        return courses.contains(course);
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

    public void saveSchedule(String filename) throws IOException {
        File savedDir = new File(SAVED_SCHEDULES_FOLDER);
        if (!savedDir.exists()) {
            savedDir.mkdirs();
        }

        Map<String, List<Course>> jsonObject = Map.of("classes", courses);
        Main.MAPPER.writeValue(new File(SAVED_SCHEDULES_FOLDER + filename), jsonObject);
    }

    public void saveSchedule() throws IOException {
        saveSchedule(SAVED_SCHEDULE);
    }

    public void addCourse(Course course) {
        courses.add(course);
    }

    public boolean removeCourse(Course course) {
        return courses.remove(course);
    }

    public List<Course> getCourses() {
        List<Course> copyCourses = new ArrayList<>();
        for (Course c : courses) {
            //if the course can't be found, don't cause a server error
            if (c == null) continue;
            List<CourseTime> timesCopy = new ArrayList<>();

            if (c.times() != null) {
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
