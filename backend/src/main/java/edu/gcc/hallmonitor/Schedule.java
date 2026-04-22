package edu.gcc.hallmonitor;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class Schedule {

    private List<Course> fallCourses;
    private List<Course> springCourses;
    private List<Course> summerCourses;
    private List<Course> winterCourses;
    private int id;
    private int userId;
    private boolean authenticated = false;
    private static final String SAVED_SCHEDULE = "saved-schedule.json";
    private static final String SAVED_SCHEDULES_FOLDER = "schedules/";
    private static final Connection CONNECTION;

    static {
        try {
            CONNECTION = Database.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Schedule(List<Course> courses) {
        fallCourses = new ArrayList<>();
        springCourses = new ArrayList<>();
        summerCourses = new ArrayList<>();
        winterCourses = new ArrayList<>();

        for(Course c : courses){ //Example semester: 2023_Fall
            getCoursesForTerm(c).add(c);
        }
    }

    public Schedule() throws SQLException {
        this(new ArrayList<Course>());
    }

    public int getId() {
        return id;
    }

    //helper method to prevent duplicate code
    public List<Course> getCoursesForTerm(Course course) {
        String semester = course.semester();

        if (semester.contains("Fall")) return fallCourses;
        if (semester.contains("Spring")) return springCourses;
        if (semester.contains("Summer")) return summerCourses;
        return winterCourses;
    }

    public boolean inSchedule(Course course){
        for (Course c: getCoursesForTerm(course)) {
            if (c.id() == course.id()) {
                return true;
            }
        }
        return false;
    }

    public boolean inSchedule(int courseId) {
        for (Course c: allCourses()) {
            if (c.id() == courseId) {
                return true;
            }
        }
        return false;
    }

    public static Schedule loadSchedule(int userId, int scheduleId) throws SQLException, JsonProcessingException {
        PreparedStatement userCheckStatement = CONNECTION.prepareStatement(
                "SELECT * FROM public.\"schedules\"" +
                    "WHERE id = ? AND user_id = ?"
        );
        userCheckStatement.setInt(1, scheduleId);
        userCheckStatement.setInt(2, userId);
        ResultSet userCheckResultSet = userCheckStatement.executeQuery();
        if (!userCheckResultSet.next()) {
            throw new SecurityException("User does not own schedule");
        }


        PreparedStatement prepStatement = CONNECTION.prepareStatement(
                "SELECT * FROM public.\"courses\"" +
                    "WHERE id IN (" +
                        "SELECT course_id FROM public.\"courses-schedules-junc\"" +
                        "WHERE schedule_id = ?" +
                    ")"
        );
        prepStatement.setInt(1, scheduleId);
        ResultSet coursesResultSet = prepStatement.executeQuery();
        Schedule schedule = new Schedule();
        schedule.id = scheduleId;
        schedule.userId = userId;

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        while (coursesResultSet.next()) {
            String[] facultyArray = (String[]) coursesResultSet.getArray("faculty").getArray();
            List<String> facultyList = Arrays.asList(facultyArray);

            String time_json = coursesResultSet.getString("times");
            List<CourseTime> courseTimes = mapper.readerForListOf(CourseTime.class).readValue(time_json);

            Course c = new Course(
                    coursesResultSet.getInt("id"),
                    coursesResultSet.getString("name"),
                    facultyList,
                    coursesResultSet.getString("subject"),
                    coursesResultSet.getInt("number"),
                    coursesResultSet.getString("section").charAt(0),
                    coursesResultSet.getString("location"),
                    coursesResultSet.getInt("credits"),
                    coursesResultSet.getString("semester"),
                    courseTimes,
                    coursesResultSet.getBoolean("is_lab"),
                    coursesResultSet.getBoolean("is_open"),
                    coursesResultSet.getInt("open_seats"),
                    coursesResultSet.getInt("total_seats")
            );
            schedule.addCourse(c);
        }

        return schedule;
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

    public void addCourse(Course course) throws SQLException {
        PreparedStatement prepStatement = CONNECTION.prepareStatement(
                "INSERT INTO public.\"courses-schedules-junc\" (schedule_id, course_id) VALUES (?, ?)"
        );
        prepStatement.setInt(1, id);
        prepStatement.setInt(2, course.id());

        getCoursesForTerm(course).add(course);
    }

    public boolean removeCourse(Course course) throws SQLException {
        PreparedStatement prepStatement = CONNECTION.prepareStatement(
                "DELETE FROM public.\"courses-schedules-junc\" WHERE schedule_id = ? AND course_id = ?"
        );
        prepStatement.setInt(1, id);
        prepStatement.setInt(2, course.id());

        return getCoursesForTerm(course).remove(course);
    }

    //check each course in the current schedule to find any overlap in time between them and course
    public String checkForOverlap(Course course){ //TODO: add unit tests
        for(Course c: getCoursesForTerm(course)){
            for(CourseTime ct : course.times()){
                int ctStartSec = ct.startTime().toSecondOfDay();
                int ctEndSec = ct.endTime().toSecondOfDay();
                if(ctEndSec <= ctStartSec) continue;
                for(CourseTime ct2 : c.times()){
                    //only perform the check if the classes are on the same day. If not, cancel checking this day and move to the next.
                    if(!ct.day().equals(ct2.day())) continue;

                    int ct2StartSec = ct2.startTime().toSecondOfDay();
                    int ct2EndSec = ct2.endTime().toSecondOfDay();
                    if(ct2EndSec <= ct2StartSec) continue;

                    if(ctStartSec < ct2EndSec && ctEndSec > ct2StartSec){
                        //if there is an overlap in the time blocks, stop checking other days and return the error message.
                        return course.department()+course.code()+course.section()+" overlaps with "+c.department()+c.code() + c.section();
                    }
                }


            }
        }
        return "";
    }

    //checks if the schedule has already saved a different section of the same class
    public boolean hasDifferentSection(Course course){ //TODO: add unit tests
        for(Course c : getCoursesForTerm(course)) {
            //if another section of the class is in the schedule, return true
            if (c.code() == course.code() && Objects.equals(c.name(), course.name()) && c.section() != course.section()) {
                return true;
            }
        }
        return false;
    }

    public List<Course> getCourses() {
        List<Course> copyCourses = new ArrayList<>();
        for (Course c : allCourses()) {
            //if the course can't be found, don't cause a server error
            if (c == null) { continue; }
            List<CourseTime> timesCopy = new ArrayList<>();

            if (c.times() != null) {
                for (CourseTime time : c.times()) {

                    timesCopy.add(new CourseTime(time.day(), time.startTime(), time.endTime()));
                }
            }

            Course copyCourse = new Course(
                    c.id(),
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
