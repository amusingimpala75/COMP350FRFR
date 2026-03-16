package edu.gcc.hallmonitor;

import java.nio.file.Files;
import java.nio.file.Path;

import io.javalin.Javalin;

public class ScheduleController {

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
    }
}
