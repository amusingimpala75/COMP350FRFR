package edu.gcc.hallmonitor;

import io.javalin.Javalin;

public class SearchController {

    //better to call this? have just one search going on
    private static Search search; //current search

    private static Schedule currentSchedule;


    public static void registerRoutes(Javalin app){
        app.post("/search", ctx -> {
                    String query = ctx.body();
                    search = new Search(query);
                }
                );
        app.get("/search", ctx ->
                ctx.json(search.getMatchResults())      //returns a json of all the courses in the search's match results
        );
//        app.post("/addCourse",  ctx-> {
//                    String courseID = ctx.body();
//                    ctx.json(currentSchedule.addCourse());
//                }
//                );

        //have one for adding/removing a filter?


    }
    public static void initSchedule(){
        currentSchedule = new Schedule();
    }
}
