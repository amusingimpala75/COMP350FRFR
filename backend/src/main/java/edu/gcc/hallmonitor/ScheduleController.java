package edu.gcc.hallmonitor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

import io.javalin.Javalin;

public class ScheduleController {
    private static int getOrCreateScheduleId(int userId) throws SQLException {
        Connection conn = Database.getConnection();
        PreparedStatement lookup = conn.prepareStatement(
            "SELECT id FROM public.\"schedules\" WHERE user_id = ? ORDER BY id LIMIT 1"
        );
        lookup.setInt(1, userId);
        ResultSet rs = lookup.executeQuery();
        if (rs.next()) {
            return rs.getInt(1);
        }

        PreparedStatement insert = conn.prepareStatement(
            "INSERT INTO public.\"schedules\" (user_id) VALUES (?)",
            Statement.RETURN_GENERATED_KEYS
        );
        insert.setInt(1, userId);
        insert.execute();
        ResultSet keys = insert.getGeneratedKeys();
        if (keys.next()) {
            return keys.getInt(1);
        }

        throw new SQLException("Failed to create schedule");
    }

    private static Integer requireUserId(io.javalin.http.Context ctx) {
        Integer userId = AuthController.getAuthenticatedUserId(ctx);
        if (userId == null) {
            ctx.status(401).result("Unauthorized");
            return null;
        }
        return userId;
    }

    public static void registerRoutes(Javalin app) {
        //Defines a /schedule route that reads from index.html
        app.get("/schedule", ctx -> ctx.html(Main.readResource("/public/index.html")));

        //adds or removes a course based on the ID
        app.post("/schedule/items", ctx -> {
            int courseId = Integer.parseInt(Objects.requireNonNull(ctx.queryParam("courseId")));
            Integer userId = requireUserId(ctx);
            if (userId == null) {
                return;
            }

            int scheduleId;
            try {
                scheduleId = getOrCreateScheduleId(userId);
            } catch (SQLException ex) {
                ctx.status(500).result("Database error");
                return;
            }

            Schedule schedule = Schedule.loadSchedule(userId, scheduleId);
            Course course = Search.getCourseById(courseId);
            String ret;
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
            Integer userId = requireUserId(ctx);
            if (userId == null) {
                return;
            }

            int scheduleId;
            try {
                scheduleId = getOrCreateScheduleId(userId);
            } catch (SQLException ex) {
                ctx.status(500).result("Database error");
                return;
            }

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
            Integer userId = requireUserId(ctx);
            if (userId == null) {
                return;
            }

            int scheduleId;
            try {
                scheduleId = getOrCreateScheduleId(userId);
            } catch (SQLException ex) {
                ctx.status(500).result("Database error");
                return;
            }
            Schedule schedule = Schedule.loadSchedule(userId, scheduleId);
            byte[] pdfBytes = schedule.createPdf();
            ctx.contentType("application/pdf");
            ctx.header("Content-Disposition", "attachment; filename=\"my-file.pdf\"");  //TODO: change filename
            ctx.result(pdfBytes);
        });
    }
}
