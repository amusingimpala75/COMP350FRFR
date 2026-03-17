package edu.gcc.hallmonitor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;

import io.javalin.Javalin;

public class SearchController {
    private static Search search = null;

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
            if (search == null || !ctx.body().equals(search.query())) {
                String query = ctx.body();
                search = new Search(query);
            }
            ctx.json(search.getMatchResults());
        });

        app.get("/search/results", ctx -> {
            ctx.json(search.getMatchResults());
        });

        app.get("/search/query", ctx -> {
            if (search == null) {
                ctx.result("");
            } else {
                ctx.result(search.query());
            }
        });

        // [TODO] there should be a better way to do this
        app.get("/courses", ctx -> { // currently in use for filters
            // create a Search object with empty query to get all courses
            // pls keep this so my frontend works -Luca
            Search allCoursesSearch = new Search("");
            ctx.json(allCoursesSearch.getMatchResults());
        });

        app.get("/search/filter", ctx -> {
            ctx.json(search.getFilters()
                    .stream()
                    .map(Filter::toJSON)
                    .collect(Collectors.toList()));
        });

        app.post("/search/filter", ctx -> {
            Filter f = Filter.fromJSON(Main.MAPPER.readTree(ctx.body()));
            search.applyFilter(f);
        });

        app.delete("/search/filter", ctx -> {
            if (ctx.queryParam("all") != null) {
                search = new Search(search.query());
            } else {
                Filter f = Filter.fromJSON(Main.MAPPER.readTree(ctx.body()));
                search.removeFilter(f);
            }
        });
    }
}
