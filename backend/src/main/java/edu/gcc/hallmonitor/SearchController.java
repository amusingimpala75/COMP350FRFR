package edu.gcc.hallmonitor;

import io.javalin.Javalin;

public class SearchController {

    private static Search search; //current search



    public static void registerRoutes(Javalin app){

        app.get("/searchPage", ctx -> {
            ctx.contentType("text/html");
            ctx.result(
                    SearchController.class.getResourceAsStream("/public/index.html")
            );
        });

        app.get("/schedulePage", ctx -> {
            ctx.contentType("text/html");
            ctx.result(
                    SearchController.class.getResourceAsStream("/public/index.html")
            );
        });

        app.get("/schedule", ctx -> {
            ctx.contentType("text/html");
            ctx.result(
                    SearchController.class.getResourceAsStream("/public/user_schedule.html")
            );
        });
        app.get("/course_search", ctx -> {
            ctx.contentType("text/html");
            ctx.result(
                    SearchController.class.getResourceAsStream("/public/index.html")
            );
        });

        app.post("/search", ctx -> {
                    String query = ctx.body();
                    search = new Search(query);
                }
                );
        app.get("/search", ctx -> {
            if (search == null) search = new Search(""); // default empty search
            ctx.json(search.getMatchResults());
        });

        app.get("/search/results", ctx -> {
            if (search == null) search = new Search("");
            ctx.json(search.getMatchResults());
        });

        app.get("/courses", ctx -> {
            // create a Search object with empty query to get all courses
            // pls keep this so my frontend works -Luca
            Search allCoursesSearch = new Search("");
            ctx.json(allCoursesSearch.getMatchResults());
        });

        //have one for adding/removing a filter?

        app.get("/scheduleItems", ctx ->
                ctx.json(Main.getCurrentSchedule().getCourses())
        );

        /** for the buttons on the courses
         */

        app.post("/inSchedule", ctx -> {
            String courseID = ctx.body();
            Schedule curr = Main.getCurrentSchedule();
            Course course = Search.getCourseByCode(courseID);
            if(curr.inSchedule(course)){
                ctx.json("True");
            }else {
                ctx.json("False");
            }
        });

        app.post("/addOrDelete",  ctx-> {
            String courseID = ctx.body();
            Schedule currSch = Main.getCurrentSchedule();
            Course course = Search.getCourseByCode(courseID);
            if(currSch.inSchedule(course)){
                currSch.removeCourse(course);
            }else {
                currSch.addCourse(Search.getCourseByCode(courseID));
            }
        });


    }

}
