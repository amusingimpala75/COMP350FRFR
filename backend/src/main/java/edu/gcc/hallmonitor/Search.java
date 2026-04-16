package edu.gcc.hallmonitor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;

import me.xdrop.fuzzywuzzy.FuzzySearch;

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
        // We only want the fall classes
        // This is a KLUDGE to make this work
        // for the MVP only!! Please change
        // when we have more time.
        allCourses.removeIf(c -> !c.semester().equals("2023_Fall"));

        courseMap = new HashMap<>();
        // hashMap with course subject+number+section pointing to the course to easily identify the courses in the schedule
        for (Course course : allCourses) {
            courseMap.put(course.department() + course.code() + course.section() + course.semester(), course);
        }

    }

    public static Course getCourseByCode(String code) {
        return courseMap.get(code); // TODO: handle problems with this
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

    public Search(String searchQuery, ArrayList<Filter> filters) {
        this.searchQuery = searchQuery;

        //sort all courses based on which match the search query best
        searchResults = allCourses.stream()
                .map(course -> {
                    String query = searchQuery.trim().toLowerCase();
                    String title = course.name().toLowerCase();
                    String dept = course.department().toLowerCase();
                    String prof = String.join(" ", course.professor()).toLowerCase();
                    String code = String.valueOf(course.code()).toLowerCase();

                    //Find the score of each attribute, allowing typos
                    //These scores are helpful when there is no exact match with the query
                    int titleScore = FuzzySearch.tokenSetRatio(title, query);
                    int deptScore = FuzzySearch.partialRatio(dept, query);
                    int profScore = FuzzySearch.partialRatio(prof, query);
                    int codeScore = FuzzySearch.partialRatio(code, query);

                    int ranking = (titleScore * 5) + deptScore + profScore + (codeScore * 2);

                    //boost aggressively for exact matches since the fuzzySearch can give high scores to unrelated strings
                    if (query.equals(title)) { ranking += 20000; }
                    if (title.contains((" " + query + " "))) { ranking += 8000; } //looking for the query as an isolated word and not a substring
                    if (title.startsWith(query)) { ranking += 4000; }
                    if (prof.equals(query)) { ranking += 20000; }
                    if (prof.contains(query)) { ranking += 8000; }
                    if (prof.startsWith(query)) { ranking += 4000; }
                    if (dept.equals(query)) { ranking += 20000; }
                    if (dept.contains(query)) { ranking += 8000; }
                    if (dept.startsWith(query)) { ranking += 4000; }
                    if (code.equals(query)) { ranking += 24000; }
                    if (code.startsWith(query)) { ranking += 4000; }

                    //attach the score to the course, sort by the score
                    return new AbstractMap.SimpleEntry<>(ranking, course);
                })
                .sorted((e1, e2) -> Integer.compare(e2.getKey(), e1.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        //matchResults is the arrayList of all courses sorted based on the query. Filters are applied to this list.
        matchResults = new ArrayList<>(searchResults);

        for (Filter f : filters) {
            this.applyFilter(f);
        }
    }

    public Search(String searchQuery) {
        this(searchQuery, new ArrayList<Filter>());
    }

    public Search() {
        searchQuery = "";
        searchResults = new ArrayList<>();
        matchResults = new ArrayList<>();
    }

    public List<Filter> getFilters() {
        return this.filterList;
    }

    //reads json and converts to courses
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
