package edu.gcc.hallmonitor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.javalin.Javalin;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

public class ScheduleController {
    public static void registerRoutes(Javalin app) {
        //Defines a /schedule route that reads from index.html
        app.get("/schedule", ctx -> ctx.html(Main.readResource("/public/index.html")));

        //adds or removes a course based on the ID
        app.post("/schedule/items", ctx -> {
            int courseId = Integer.parseInt(Objects.requireNonNull(ctx.queryParam("courseId")));
            int userId = Integer.parseInt(Objects.requireNonNull(ctx.queryParam("userId")));
            int scheduleId = Integer.parseInt(Objects.requireNonNull(ctx.queryParam("scheduleId")));

            Schedule schedule = Schedule.loadSchedule(userId, scheduleId);
            Course course = Search.getCourseById(courseId);
            String ret = "";
            if (schedule.inSchedule(course)) {
                schedule.removeCourse(course);
                ret = "Removed";
            } else {
                if (schedule.hasDifferentSection(course)) {
                    //check for a different section of the class
                    ret = "Already scheduled for a different section of this class";
                } else {
                    //check for overlap
                    String overlap = schedule.checkForOverlap(course);
                    if (!Objects.equals(overlap, "")) {
                        ret = overlap;
                    } else {
                        //doesn't overlap, not scheduled for a different section
                        schedule.addCourse(course);
                        ret = "Added";
                    }

                }
            }

            ctx.result(ret);

        });

        // Get the schedule that is saved
        app.get("/schedule/items", ctx -> {
            String term = ctx.queryParam("term"); // Fall, Winter, Spring, Summer
            int userId = Integer.parseInt(Objects.requireNonNull(ctx.queryParam("userId")));
            int scheduleId = Integer.parseInt(Objects.requireNonNull(ctx.queryParam("scheduleId")));

            Schedule schedule = Schedule.loadSchedule(userId, scheduleId);
            if (term == null || term.isBlank()) {
                ctx.json(schedule.getCourses());
                return;
            }
            ctx.json(schedule.getCourses().stream()
                .filter(course -> course.semester() != null && course.semester().contains(term))
                .toList());
        });

        //returns the byte array of the schedules for the pdf
        app.get("/download-pdf", ctx -> {
            int userId = Integer.parseInt(Objects.requireNonNull(ctx.queryParam("userId")));
            int scheduleId = Integer.parseInt(Objects.requireNonNull(ctx.queryParam("scheduleId")));
            Schedule schedule = Schedule.loadSchedule(userId, scheduleId);
            byte[] pdfBytes = schedule.createPdf();
            ctx.contentType("application/pdf");
            ctx.header("Content-Disposition", "attachment; filename=\"my-file.pdf\"");  //TODO: change filename
            ctx.result(pdfBytes);
        });
    }
}
