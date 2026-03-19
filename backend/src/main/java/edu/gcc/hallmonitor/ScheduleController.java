package edu.gcc.hallmonitor;

import java.nio.file.Files;
import java.nio.file.Path;
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
     * @return byte array containing the schedule pdf information
     * @throws IOException
     */
    private static byte[] createPdf() throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDPage page = new PDPage();
            document.addPage(page);

            //TODO: eventually change to distinguish fall and spring
            //get courses from current schedule
            Schedule schedule = Schedule.loadSchedule();
            String body = "";
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            for(Course c : schedule.getCourses()){
                body = body + c.department() + c.code() + c.section() + " " + c.name() + "\n";
                body += "      " + String.join(" ", c.professor()) + "\n";
                body += "      ";
//                String days = "";  //group it by the days with the same class times?
//                String times = "";
                if(c.times() != null){
                    for(CourseTime ct : c.times()){
                        //days = days +       //ct.endTime().minusSeconds()
                        body += ct.day() + ct.startTime().format(formatter) + " - " +  ct.endTime().format(formatter) + "   ";
                    }
                }
                body += "\n";
            }

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.beginText();
                content.setFont(
                        new PDType1Font(Standard14Fonts.FontName.HELVETICA),
                        12
                );
                content.newLineAtOffset(100, 700);
                for (String line : body.split("\n")) {
                    content.showText(line);
                    content.newLineAtOffset(0, -20);
                }
                content.endText();
            }

            document.save(out);
            return out.toByteArray();
        }
    }

    public static void registerRoutes(Javalin app) {
        app.get("/schedule", ctx -> ctx.html(
            Files.readString(
                Path.of(
                    ScheduleController.class
                            .getResource("/public/index.html")
                            .toURI()
                )
            )
        ));

        app.post("/schedule/items", ctx -> {
            Schedule schedule = Schedule.loadSchedule();

            String courseID = ctx.body();

            Course course = Search.getCourseByCode(courseID);
            String ret = "";

            if (schedule.inSchedule(course)) {
                schedule.removeCourse(course);
                ret = "Removed";
            }else{
               List<Course> courses = schedule.getCourses();
               for(Course c : courses) {
                   //if already scheduled for a different section of the class, end the loop and return the string explaining the error
                   if (c.code() == course.code() && Objects.equals(c.name(), course.name()) && c.section() != course.section()) {
                       ret = "already scheduled for a different section of this class";
                       break;
                   }

                   //check each class time in schedule to see if the times overlap
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
                               ret = "Course " + course.department() + course.code() + course.section() + " overlaps with " + c.department() + c.code() + c.section();
                               break;
                           }
                       }


                   }

               }

               //if there is no conflict with the schedule courses
                if(ret.isEmpty()){
                    schedule.addCourse(Search.getCourseByCode(courseID));
                    ret = "Added";
                }
            }


            schedule.saveSchedule();

            ctx.result(ret);

        });

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
