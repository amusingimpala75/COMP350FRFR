package edu.gcc.hallmonitor;

import io.javalin.Javalin;

public class ScheduleController {

    private static Schedule schedule; //current schedule



    public static void registerRoutes(Javalin app){
        app.post("/schedule", ctx -> {
                    schedule.saveSchedule();
                }
        );
        app.get("/schedule", ctx -> {
                    schedule = Schedule.loadSchedule();
                    ctx.json(schedule); // Return the loaded schedule
                }

        );
    }
}
