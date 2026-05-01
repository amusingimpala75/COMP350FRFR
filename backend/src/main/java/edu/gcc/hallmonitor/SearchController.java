package edu.gcc.hallmonitor;

import java.util.stream.Collectors;

import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.List;

public class SearchController {
    private static Search search = new Search();

    private static Search getSearch(Context ctx) {
        Search search = ctx.sessionAttribute("search");
        if (search == null) {
            search = new Search();
            setSearch(ctx, search);
        }
        return search;
    }

    private static void setSearch(Context ctx, Search search) {
        ctx.sessionAttribute("search", search);
    }

    public static void registerRoutes(Javalin app) {
        // Get the search page
        app.get("/search", ctx -> ctx.html(Main.readResource("/public/index.html")));

        //creates and returns results from a new search object based on the user's query and filter selections
        app.post("/search", ctx -> {
            String query = ctx.body();
            Search old = getSearch(ctx);
            Search search = new Search(query);
            old.getFilters().forEach(search::applyFilter);
            setSearch(ctx, search);
            ctx.json(search.getMatchResults());
        });

        // Get the previous search results
        app.get("/search/results", ctx -> {
            ctx.json(getSearch(ctx).getMatchResults());
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
            ctx.json(getSearch(ctx).getFilters()
                    .stream()
                    .map(Filter::toJSON)
                    .collect(Collectors.toList()));
        });

        // Add a filter
        app.post("/search/filter", ctx -> {
            Filter f = Filter.fromJSON(Main.MAPPER.readTree(ctx.body()));
            Search search = getSearch(ctx);
            search.applyFilter(f);
            setSearch(ctx, search);
        });

        // Delete a filter
        app.delete("/search/filter", ctx -> {
            if (ctx.queryParam("all") != null) {
                Search search = new Search(getSearch(ctx).query());
                setSearch(ctx, search);
            } else {
                Filter f = Filter.fromJSON(Main.MAPPER.readTree(ctx.body()));
                Search search = getSearch(ctx);
                search.removeFilter(f);
                setSearch(ctx, search);
            }
        });

        // Reset all search state (query, filters, and results)
        app.post("/search/reset", ctx -> {
            setSearch(ctx, new Search(""));
            ctx.status(204);
        });

        app.get("/search/filter-values/{filter-type}", ctx -> {
            List<Course> courses = getSearch(ctx).getMatchResults();
            if (courses.isEmpty()) {
                courses = Search.allCourses;
            }
            ctx.json(Filter.possibleValues(ctx.pathParam("filter-type"), courses));
        });
    }
}
