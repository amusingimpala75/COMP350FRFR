package edu.gcc.hallmonitor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import io.javalin.Javalin;

public class ScheduleController {

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

            //remove the course if it's already present in the schedule
            if (schedule.inSchedule(course)) {
                schedule.removeCourse(course);
                ret = "Removed";
            }else{
               List<Course> courses = schedule.getCourses();
               for(Course c : courses) {
                   //if another section of the class is in the schedule, end the loop and return the string explaining the error
                   if (c.code() == course.code() && Objects.equals(c.name(), course.name()) && c.section() != course.section()) {
                       ret = "Already scheduled for a different section of this class";
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
    }
}
