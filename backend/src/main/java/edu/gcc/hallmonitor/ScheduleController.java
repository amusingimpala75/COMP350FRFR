package edu.gcc.hallmonitor;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.javalin.Javalin;
import io.javalin.http.Context;

public class ScheduleController {
    private static Integer requireUserId(io.javalin.http.Context ctx) {
        Integer userId = AuthController.getAuthenticatedUserId(ctx);
        if (userId == null) {
            ctx.status(401).result("Unauthorized");
            return null;
        }
        return userId;
    }

    private static Schedule resolveSchedule(Context ctx, int userId, boolean createIfMissing) {
        int scheduleId;
        String scheduleIdParam = ctx.queryParam("scheduleId");

        try {
            if (scheduleIdParam != null && !scheduleIdParam.isBlank()) {
                scheduleId = Integer.parseInt(scheduleIdParam);
            } else if (createIfMissing) {
                scheduleId = Schedule.getOrCreateScheduleId(userId);
            } else {
                ctx.status(400).result("Missing scheduleId");
                return null;
            }
        } catch (NumberFormatException ex) {
            ctx.status(400).result("Invalid scheduleId");
            return null;
        } catch (SQLException ex) {
            ctx.status(500).result("Database error");
            return null;
        }

        try {
            return Schedule.loadSchedule(userId, scheduleId);
        } catch (SecurityException ex) {
            if (!createIfMissing) {
                ctx.status(404).result("Schedule not found");
                return null;
            }

            // Recover from stale/foreign schedule ids by switching to the user's default schedule.
            try {
                int fallbackScheduleId = Schedule.getOrCreateScheduleId(userId);
                return Schedule.loadSchedule(userId, fallbackScheduleId);
            } catch (Exception fallbackEx) {
                ctx.status(500).result("Database error");
                return null;
            }
        } catch (Exception ex) {
            ctx.status(500).result("Database error");
            return null;
        }
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

            Schedule schedule = resolveSchedule(ctx, userId, true);
            if (schedule == null) {
                return;
            }
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

        // get schedules for user
        app.get("/schedules", ctx -> {
            Integer userId = requireUserId(ctx);
            if (userId == null) {
                return;
            }
            User user = new User(userId);
            List<Schedule> schedules = user.getSchedules();
            List<ScheduleDTO> scheduleDTOs = schedules.stream().map(
                    schedule -> new ScheduleDTO(schedule.getId(), schedule.getName())
            ).toList();

            ctx.json(scheduleDTOs);
        });

        // add a new schedule
        app.post("/schedule", ctx -> {
            Integer userId = requireUserId(ctx);
            if (userId == null) {
                return;
            }
            String name = Objects.requireNonNull(ctx.queryParam("scheduleName"));

            try {
                int scheduleId = Schedule.newSchedule(userId, name);
                ctx.json(new ScheduleDTO(scheduleId, name));
            } catch (SecurityException se) {
                ctx.status(409); // Duplicate resource
                ctx.json(Map.of("error", "schedule with the same name already exists"));
            }

        });

        app.delete("/schedule", ctx -> {
            Integer userId = requireUserId(ctx);
            if (userId == null) {
                return;
            }
            int scheduleId = Integer.parseInt(Objects.requireNonNull(ctx.queryParam("scheduleId")));

            try {
                User user = new User(userId);
                user.removeSchedule(scheduleId);
            } catch (SecurityException se) {
                ctx.status(404); // schedule not found for user
            } catch (IllegalStateException ise) {
                ctx.status(400); // user only has 1 schedule
            }
        });

        // Get the schedule for the uesrid and scheduleid given
        app.get("/schedule/items", ctx -> {
            String term = ctx.queryParam("term"); // Fall, Winter, Spring, Summer
            Integer userId = requireUserId(ctx);
            if (userId == null) {
                return;
            }

            Schedule schedule = resolveSchedule(ctx, userId, true);
            if (schedule == null) {
                return;
            }
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

            Schedule schedule = resolveSchedule(ctx, userId, true);
            if (schedule == null) {
                return;
            }
            byte[] pdfBytes = schedule.createPdf();
            ctx.contentType("application/pdf");
            ctx.header("Content-Disposition", "attachment; filename=\"my-file.pdf\"");  //TODO: change filename
            ctx.result(pdfBytes);
        });
    }
}
