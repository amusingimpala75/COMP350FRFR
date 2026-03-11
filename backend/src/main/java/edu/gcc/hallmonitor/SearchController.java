package edu.gcc.hallmonitor;

import io.javalin.Javalin;

public class SearchController {

    private static Search search; //current search



    public static void registerRoutes(Javalin app){
        app.post("/search", ctx -> {
                    String query = ctx.body();
                    search = new Search(query);
                }
                );
        app.get("/search", ctx ->
                ctx.json(search.getMatchResults())      //returns a json of all the courses in the search's match results
        );

        app.get("/scheduleItems", ctx ->
                ctx.json(Main.getCurrentSchedule().getCourses())
        );

        app.post("/addOrDelete",  ctx-> {
                    String courseID = ctx.body();
                    Schedule currSch = Main.getCurrentSchedule();
                    Course course = Search.getCourseByCode(courseID);
                    if(currSch.inSchedule(course)){
                        currSch.removeCourse(course);
                    }else {
                        currSch.addCourse(Search.getCourseByCode(courseID));
                    }
                }
                );

//        app.delete("/delete/{code}", ctx -> {
//            String courseID = ctx.pathParam("code");
//            Schedule currSch = Main.getCurrentSchedule();
//            boolean removed = currSch.removeCourse(Search.getCourseByCode(courseID));
//
//            if (removed) {
//                ctx.status(204); // success, no body
//            } else {
//                ctx.status(404);
//            }
//        });

        //have one for adding/removing a filter?


    }

}
