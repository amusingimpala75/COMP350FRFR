package edu.gcc.hallmonitor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Schedule {

    private List<Course> courses;
    private static final String SAVED_SCHEDULE = "src/main/saved_schedules/saved-schedule.json";

    public Schedule(List<Course> courses) {
        this.courses = courses;
    }
    public Schedule() {

    }

    public static Schedule loadSchedule() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode root = mapper.readTree(new File(SAVED_SCHEDULE));
        JsonNode classesNode = root.get("classes"); // Grab the courses array inside the json

        return new Schedule(mapper.readerForListOf(Course.class).readValue(classesNode));
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
