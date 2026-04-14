package edu.gcc.hallmonitor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.time.format.DateTimeFormatter;

import io.javalin.Javalin;

public class ScheduleController {

    /**
     * allows line wrapping in a pdf based on the length of a line and the width of the page
     * @param text the line
     * @param font the font used
     * @param fontSize
     * @param maxWidth the width of the page
     * @return a list of the lines, each one fitting the page width
     * @throws IOException
     */
    private static List<String> wrapText(String text, PDType1Font font, float fontSize, float maxWidth) throws IOException {
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

    private static byte[] createPdf() throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDPage page = new PDPage();
            document.addPage(page);

            //get courses from current schedule
            Schedule schedule = Schedule.loadSchedule();
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
            for (Course c : schedule.getCourses()) {

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

    public static void registerRoutes(Javalin app) {
        //Defines a /schedule route that reads from index.html
        app.get("/schedule", ctx -> ctx.html(
            Files.readString(
                Path.of(
                    ScheduleController.class
                            .getResource("/public/index.html")
                            .toURI()
                )
            )
        ));

        //adds or removes a course based on the ID
        app.post("/schedule/items", ctx -> {
            Schedule schedule = Schedule.loadSchedule();
            String courseID = ctx.body();

            Course course = Search.getCourseByCode(courseID);
            String ret = "";

            // remove the course if it's already present in the schedule
            if (schedule.inSchedule(course)) {
                schedule.removeCourse(course);
                ret = "Removed";
            } else {
                List<Course> courses = schedule.getCourses();
                for (Course c : courses) {
                    //if another section of the class is in the schedule, end the loop and return the string explaining the error
                    if (c.code() == course.code() && Objects.equals(c.name(), course.name()) && c.section() != course.section()) {
                        ret = "Already scheduled for a different section of this class";
                        break;
                    }

                    //check each class time in schedule to see if the times overlap
                    for (CourseTime ct : course.times()) {
                        int ctStartSec = ct.startTime().toSecondOfDay();
                        int ctEndSec = ct.endTime().toSecondOfDay();
                        if (ctEndSec <= ctStartSec) { continue; }
                        for (CourseTime ct2 : c.times()) {
                            //only perform the check if the classes are on the same day. If not, cancel checking this day and move to the next.
                            if (!ct.day().equals(ct2.day())) { continue; }

                            int ct2StartSec = ct2.startTime().toSecondOfDay();
                            int ct2EndSec = ct2.endTime().toSecondOfDay();
                            if (ct2EndSec <= ct2StartSec) { continue; }

                            if (ctStartSec < ct2EndSec && ctEndSec > ct2StartSec) {
                                //if there is an overlap in the time blocks, stop checking other days and return the error message.
                                ret = "Course " + course.department() + course.code() + course.section() + " overlaps with " + c.department() + c.code() + c.section();
                                break;
                            }
                        }


                    }

                }

                //if there is no conflict with the schedule courses
                if (ret.isEmpty()) {
                    schedule.addCourse(Search.getCourseByCode(courseID));
                    ret = "Added";
                }
            }


            schedule.saveSchedule();

            ctx.result(ret);

        });

        // Get the schedule that is saved
        app.get("/schedule/items", ctx -> {
            ctx.json(Schedule.loadSchedule().getCourses()); // Return the loaded schedule as a json
        });

        //returns the byte array of the schedules for the pdf
        app.get("/download-pdf", ctx -> {
            byte[] pdfBytes = createPdf();
            ctx.contentType("application/pdf");
            ctx.header("Content-Disposition", "attachment; filename=\"my-file.pdf\"");  //TODO: change filename
            ctx.result(pdfBytes);
        });
    }
}
