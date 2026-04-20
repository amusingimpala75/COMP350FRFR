package edu.gcc.hallmonitor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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
        allCourses = new ArrayList<>();
        //if this is the first search, initialize the allCourses list
        try {
            Connection conn = Database.getConnection();
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM public.\"courses\"");

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            while (rs.next()) {
                String[] facultyArray = (String[]) rs.getArray("faculty").getArray();
                List<String> facultyList = Arrays.asList(facultyArray);

                String time_json = rs.getString("times");
                List<CourseTime> courseTimes = mapper.readerForListOf(CourseTime.class).readValue(time_json);

                Course c = new Course(
                        rs.getString("name"),
                        facultyList,
                        rs.getString("subject"),
                        rs.getInt("number"),
                        rs.getString("section").charAt(0),
                        rs.getString("location"),
                        rs.getInt("credits"),
                        rs.getString("semester"),
                        courseTimes,
                        rs.getBoolean("is_lab"),
                        rs.getBoolean("is_open"),
                        rs.getInt("open_seats"),
                        rs.getInt("total_seats")
                );
                allCourses.add(c);
            }
        } catch (SQLException sqle) {
            System.err.println("Error connecting to database: " + sqle.getMessage());
            sqle.printStackTrace();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        //want 2023_Fall, 2023_Winter_Online, 2024_Spring, 2024_Early_Summer (remove 2024_Fall and 2023_Spring)
        allCourses.removeIf(c -> (c.semester().equals("2023_Spring") ||
            c.semester().equals("2024_Fall")));

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

    public String query() {
        return this.searchQuery;
    }
}
