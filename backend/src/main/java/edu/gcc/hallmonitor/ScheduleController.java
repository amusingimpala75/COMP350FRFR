package edu.gcc.hallmonitor;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.javalin.Javalin;

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

        // get schedules for user
        app.get("/schedules", ctx -> {
            int userId = Integer.parseInt(Objects.requireNonNull(ctx.queryParam("userId")));
            User user = new User(userId);
            List<Schedule> schedules = user.getUserSchedules();
            List<ScheduleDTO> scheduleDTOs = schedules.stream().map(
                    schedule -> new ScheduleDTO(schedule.getId(), schedule.getName())
            ).toList();

            ctx.json(scheduleDTOs);
        });

        // add a new schedule
        app.post("/schedule", ctx -> {
            int userId = Integer.parseInt(Objects.requireNonNull(ctx.queryParam("userId")));
            String name = Objects.requireNonNull(ctx.queryParam("scheduleName"));

            try {
                int scheduleId = Schedule.newSchedule(userId, name);
                ctx.json(new ScheduleDTO(scheduleId, name));
            } catch (SecurityException se) {
                ctx.status(409); // Duplicate resource
                ctx.json(Map.of("error", "schedule with the same name already exists"));
            }

        });

        // Get the schedule for the uesrid and scheduleid given
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
