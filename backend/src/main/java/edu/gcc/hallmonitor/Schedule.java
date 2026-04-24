package edu.gcc.hallmonitor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

public class Schedule {

    private List<Course> fallCourses;
    private List<Course> springCourses;
    private List<Course> summerCourses;
    private List<Course> winterCourses;
    private int id;
    private int userId;
    private String name;
    private boolean authenticated = false;
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
            addCourseInMemory(c);
        }
    }

    public Schedule(String name) {
        this(new ArrayList<Course>());
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() { return name; }

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
        Schedule schedule;
        PreparedStatement userCheckStatement = CONNECTION.prepareStatement(
                "SELECT * FROM public.\"schedules\" " +
                    "WHERE id = ? AND user_id = ?"
        );
        userCheckStatement.setInt(1, scheduleId);
        userCheckStatement.setInt(2, userId);
        ResultSet userCheckResultSet = userCheckStatement.executeQuery();
        if (!userCheckResultSet.next()) {
            throw new SecurityException("User does not own schedule");
        } else {
            schedule = new Schedule(
                userCheckResultSet.getString("name")
            );
        }

        PreparedStatement prepStatement = CONNECTION.prepareStatement(
                "SELECT * FROM public.\"courses\" " +
                    "WHERE id IN (" +
                        "SELECT course_id FROM public.\"courses-schedules-junc\" " +
                        "WHERE schedule_id = ?" +
                    ")"
        );
        prepStatement.setInt(1, scheduleId);
        ResultSet coursesResultSet = prepStatement.executeQuery();

        schedule.id = scheduleId;
        schedule.userId = userId;
        schedule.authenticated = true;

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
            schedule.addCourseInMemory(c);
        }

        return schedule;
    }

    public static int newSchedule(int userId, String name) throws SQLException {
        // Ensure that the name isn't already taken for the user
        PreparedStatement nameCheckStatement = CONNECTION.prepareStatement(
                "SELECT name FROM public.\"schedules\" WHERE user_id = ? AND name = ?"
        );
        nameCheckStatement.setInt(1, userId);
        nameCheckStatement.setString(2, name.trim().toLowerCase());
        ResultSet rs = nameCheckStatement.executeQuery();
        if (rs.next()) {
            throw new SecurityException("User cannot have two schedules with the same name");
        }

        PreparedStatement prepStatement = CONNECTION.prepareStatement(
                "INSERT INTO public.\"schedules\" (user_id, name) VALUES (?, ?)",
                PreparedStatement.RETURN_GENERATED_KEYS
        );
        prepStatement.setInt(1, userId);
        prepStatement.setString(2, name);
        prepStatement.execute();

        ResultSet generatedKeys = prepStatement.getGeneratedKeys();
        if (generatedKeys.next()) {
            return generatedKeys.getInt(1);
        } else {
            throw new SecurityException("Unable to generate new schedule");
        }
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
        if (!authenticated) {
            throw new SecurityException("Unauthenticated user");
        }

        PreparedStatement prepStatement = CONNECTION.prepareStatement(
                "INSERT INTO public.\"courses-schedules-junc\" (schedule_id, course_id) VALUES (?, ?)"
        );
        prepStatement.setInt(1, id);
        prepStatement.setInt(2, course.id());
        prepStatement.execute();

        addCourseInMemory(course);
    }

    public void addCourseInMemory(Course course) {
        getCoursesForTerm(course).add(course);
    }

    public boolean removeCourse(Course course) throws SQLException {
        if (!authenticated) {
            throw new SecurityException("Unauthenticated user");
        }

        PreparedStatement prepStatement = CONNECTION.prepareStatement(
                "DELETE FROM public.\"courses-schedules-junc\" WHERE schedule_id = ? AND course_id = ?"
        );
        prepStatement.setInt(1, id);
        prepStatement.setInt(2, course.id());
        prepStatement.execute();

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

    /**
     * allows line wrapping in a pdf based on the length of a line and the width of the page
     * @param text the line
     * @param font the font used
     * @param fontSize
     * @param maxWidth the width of the page
     * @return a list of the lines, each one fitting the page width
     * @throws IOException
     */
    private List<String> wrapText(String text, PDType1Font font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        text = text.replace("\n", " ");
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            //for each word, make sure it fits on its current line
            String testLine = line.isEmpty() ? word : line + " " + word;
            float size = font.getStringWidth(testLine) / 1000 * fontSize;

            //if the word doesn't fit on the line, create a new line and continue the words
            if (size > maxWidth) {
                lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(testLine);
            }
        }

        if (!line.isEmpty()) {
            lines.add(line.toString());
        }

        return lines;
    }

    public byte[] createPdf() throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDPage page = new PDPage();
            document.addPage(page);

            //get courses from current schedule
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

            //define how the page should be styled
            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            float fontSize = 12;
            float margin = 50;
            float yStart = 700;
            float y = yStart;
            float leading = 18;
            float maxWidth = page.getMediaBox().getWidth() - 2 * margin;

            //begin adding text
            PDPageContentStream content = new PDPageContentStream(document, page);
            content.beginText();
            content.setFont(font, fontSize);
            content.newLineAtOffset(margin, yStart);

            //for each course, add a first (non-indented) line with the dept, code, section, and title. On the next lines, add the professor, location, and times.
            for (Course c : getCourses()) {  //TODO change the PDF to reflect the different semesters

                //lines without indent
                String first = c.department() + c.code() + c.section() + " " + c.name();
                for (String line : wrapText(first, font, fontSize, maxWidth)) {

                    //add a new page if necessary
                    if (y < margin) {
                        content.endText();
                        content.close();

                        page = new PDPage();
                        document.addPage(page);

                        content = new PDPageContentStream(document, page);
                        content.beginText();
                        content.setFont(font, fontSize);
                        content.newLineAtOffset(margin, yStart);

                        y = yStart;
                    }

                    //add the line content to the page
                    content.showText(line);
                    content.newLineAtOffset(0, -leading);
                    y -= leading;
                }

                //lines with indent
                List<String> lowerLines = new ArrayList<>();
                String second = String.join(" ", c.professor()) + " " + c.location();
                StringBuilder third = new StringBuilder();
                if (c.times() != null) {
                    for (CourseTime ct : c.times()) {
                        third.append(ct.day()).append(" ").append(ct.startTime().format(formatter)).append(" - ").append(ct.endTime().format(formatter)).append("   ");
                    }
                }
                lowerLines.addAll(wrapText(second, font, fontSize, maxWidth));
                lowerLines.addAll(wrapText(third.toString(), font, fontSize, maxWidth));

                //separate loop needed for indented lines
                for (String line : lowerLines) {
                    //add a new page if necessary
                    if (y < margin) {
                        content.endText();
                        content.close();

                        page = new PDPage();
                        document.addPage(page);

                        content = new PDPageContentStream(document, page);
                        content.beginText();
                        content.setFont(font, fontSize);
                        content.newLineAtOffset(margin, yStart);

                        y = yStart;
                    }

                    content.newLineAtOffset(20, 0);
                    content.showText(line);
                    content.newLineAtOffset(-20, 0);
                    content.newLineAtOffset(0, -leading);
                    y -= leading;
                    //add a newline if last line
                    if (line.equals(lowerLines.get(lowerLines.size() - 1))) {
                        content.newLineAtOffset(0, -leading);
                        y -= leading;
                    }
                }
            }
            content.endText();
            content.close();

            document.save(out);
            return out.toByteArray();
        }
    }
}
