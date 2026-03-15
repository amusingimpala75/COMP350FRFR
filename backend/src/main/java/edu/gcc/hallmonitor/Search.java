package edu.gcc.hallmonitor;

import com.fasterxml.jackson.databind.JsonNode;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import java.util.HashMap;


import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

public class Search {

    private String searchQuery;
    private List<Filter> filterList = new ArrayList<>();
    private List<Course> searchResults;
    private List<Course> matchResults;

    private static List<Course> allCourses;
    private static HashMap<String, Course> courseMap;

    static {
        loadCourses();
    }

    private static void loadCourses() {
        //if this is the first search, initialize the allCourses list
        try {
            allCourses = loadData("courses.json");
        } catch (FileNotFoundException fnfe) {
            System.err.println("File not found: " + fnfe.getMessage());
        } catch (IOException ioe) {
            System.err.println("IO Exception occurred: " + ioe.getMessage());
        }

        courseMap = new HashMap<>();
        //make hashMap with course subject+number+section pointing to the course
        for(Course course : allCourses){
            courseMap.put(course.department()+course.code()+course.section(),course);
        }

    }

    public static Course getCourseByCode(String code){
        return courseMap.get(code); //TODO: handle problems with this
    }

    public void applyFilter(Filter filter) {
        this.filterList.add(filter);
        matchResults = matchResults
                .stream()
                .filter(filter)
                .collect(Collectors.toList());
    }

    public void removeFilter(Filter filter) {
        List<Filter> copy = new ArrayList<>(filterList);
        copy.remove(filter);

        matchResults = new ArrayList<>(searchResults);
        filterList = new ArrayList<>();

        for (Filter f : copy) {
            applyFilter(f);
        }
    }

    public List<Course> getMatchResults() {
        return matchResults;
    }

    // The constructors will search the db for the appropriate courses
    public Search(String searchQuery, ArrayList<Filter> filters) {
        this.searchQuery = searchQuery;

        searchResults = allCourses.stream()
                .map(course -> {
                    // compare the string of all the relevant attributes of
                    // the course to the query string
                    // TODO: change department to be a string of
                    // all the necessary parameters

                    String[] searchFields = {
                        course.department(),
                        course.name(),
                        String.join(" ", course.professor()),
                        String.valueOf(course.code())
                    };
                    String searchSpace = String.join(" ", searchFields);
                    int ranking = FuzzySearch.tokenSetPartialRatio(searchSpace, searchQuery);
                    return new AbstractMap.SimpleEntry<>(ranking, course);
                })
                .sorted((e1, e2) -> Integer.compare(e2.getKey(), e1.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        //applying filters will just need to take the searchresults returned by this code ^^ and only add the matching courses to the matchResults list
        matchResults = new ArrayList<>(searchResults);

        for (Filter f : filters) {
            this.applyFilter(f);
        }
    }

    public Search(String searchQuery) {
        this(searchQuery, new ArrayList<Filter>());
    }

    public List<Filter> getFilters() {
        return this.filterList;
    }

    public static List<Course> loadData(String coursesFilename) throws IOException {
        URL jsonURL = Main.class.getResource(String.format("/%s", coursesFilename));
        if (jsonURL == null) {
            throw new FileNotFoundException(String.format("Could not find '%s' in resources directory", coursesFilename));
        }

        JsonNode root = Main.MAPPER.readTree(jsonURL);
        JsonNode classesNode = root.get("classes"); // Grab the courses array inside the json

        return Main.MAPPER.readerForListOf(Course.class).readValue(classesNode);
    }

    public String query() {
        return this.searchQuery;
    }
}
