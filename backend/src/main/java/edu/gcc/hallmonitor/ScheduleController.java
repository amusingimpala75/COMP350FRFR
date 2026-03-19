package edu.gcc.hallmonitor;

import java.nio.file.Files;
import java.nio.file.Path;
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
                body += "      ";
//                String days = "";
//                String times = "";
                if(c.times() != null){
                    for(CourseTime ct : c.times()){
                        //days = days +       //ct.endTime().minusSeconds()
                        body += ct.day() + ct.startTime().format(formatter) + " - " +  ct.endTime().format(formatter) + "   ";
                    }
                }
            }

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.beginText();
                content.setFont(
                        new PDType1Font(Standard14Fonts.FontName.HELVETICA),
                        12
                );
                content.newLineAtOffset(100, 700);
                content.showText(body);
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
            if (schedule.inSchedule(course)) {
                schedule.removeCourse(course);
            } else {
                schedule.addCourse(Search.getCourseByCode(courseID));
            }

            schedule.saveSchedule();
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
