package edu.gcc.hallmonitor;

import java.nio.file.Files;
import java.nio.file.Path;

import io.javalin.Javalin;

public class SearchController {
    private static Search search = new Search("");

    public static void registerRoutes(Javalin app) {
        app.get("/search", ctx -> ctx.html(
            Files.readString(
                Path.of(
                    SearchController.class
                            .getResource("/public/index.html")
                            .toURI()
                )
            )
        ));


        app.post("/search", ctx -> {
            if (!ctx.body().equals(search.query())) {
                String query = ctx.body();
                search = new Search(query);
            }
            ctx.json(search.getMatchResults());
        });

        // [TODO] there should be a better way to do this
        app.get("/courses", ctx -> {
            // create a Search object with empty query to get all courses
            // pls keep this so my frontend works -Luca
            Search allCoursesSearch = new Search("");
            ctx.json(allCoursesSearch.getMatchResults());
        });

        // [TODO] have post for adding/removing a filter
    }
}
