package edu.gcc.hallmonitor;

import java.util.stream.Collectors;

import io.javalin.Javalin;
import java.util.List;

public class SearchController {
    private static Search search = new Search();

    private static void resetSearchState() {
        search = new Search("");
    }

    public static void registerRoutes(Javalin app) {
        // Get the search page
        app.get("/search", ctx -> ctx.html(Main.readResource("/public/index.html")));

        //creates and returns results from a new search object based on the user's query and filter selections
        app.post("/search", ctx -> {
            String query = ctx.body();
            Search old = search;
            search = new Search(query);
            old.getFilters().forEach(search::applyFilter);
            ctx.json(search.getMatchResults());
        });

        // Get the previous search results
        app.get("/search/results", ctx -> {
            ctx.json(search.getMatchResults());
        });

        // Get the previous search query
        app.get("/search/query", ctx -> {
            ctx.result(search.query());
        });

        // [TODO] there should be a better way to do this
        // Get the available course information for filters
        app.get("/courses", ctx -> { // currently in use for filters
            // create a Search object with empty query to get all courses
            // pls keep this so my frontend works -Luca
            Search allCoursesSearch = new Search("");
            ctx.json(allCoursesSearch.getMatchResults());
        });

        // Get the previous filters
        app.get("/search/filter", ctx -> {
            ctx.json(search.getFilters()
                    .stream()
                    .map(Filter::toJSON)
                    .collect(Collectors.toList()));
        });

        // Add a filter
        app.post("/search/filter", ctx -> {
            Filter f = Filter.fromJSON(Main.MAPPER.readTree(ctx.body()));
            search.applyFilter(f);
        });

        // Delete a filter
        app.delete("/search/filter", ctx -> {
            if (ctx.queryParam("all") != null) {
                search = new Search(search.query());
            } else {
                Filter f = Filter.fromJSON(Main.MAPPER.readTree(ctx.body()));
                search.removeFilter(f);
            }
        });

        // Reset all search state (query, filters, and results)
        app.post("/search/reset", ctx -> {
            resetSearchState();
            ctx.status(204);
        });

        app.get("/search/filter-values/{filter-type}", ctx -> {
            List<Course> courses = search.getMatchResults();
            if (courses.isEmpty()) {
                courses = Search.allCourses;
            }
            ctx.json(Filter.possibleValues(ctx.pathParam("filter-type"), courses));
        });
    }
}
