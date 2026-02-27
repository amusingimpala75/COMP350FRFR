package edu.gcc.hallmonitor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kotlin.Pair;
import me.xdrop.fuzzywuzzy.FuzzySearch;

import java.lang.reflect.Array;
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
    private ArrayList<Filter> filterList;
    private List<Course> searchResults;
    private ArrayList<Course> matchResults;
    private static List<Course> allCourses;

    public void applyFilter(Filter filter) {

    }

    public void removeFilter(Filter filter) {

    }

    public ArrayList<Course> getMatchResults() {
        //TODO: make a /results route and use this method to convert into json
        return matchResults;
    }

    public static void main(String[] args) {
        Search s = new Search("COMP");
    }

    //The constructors will search the db for the appropriate courses
    public Search(String searchQuery) {
        this.searchQuery = searchQuery;
        //if this is the first search, initialize the allCourses list
        if(allCourses == null){
            try {
                allCourses = loadData("courses.json");
            } catch (FileNotFoundException fnfe) {
                System.err.println("File not found: " + fnfe.getMessage());
            } catch (IOException ioe) {
                System.err.println("IO Exception occurred: " + ioe.getMessage());
            }
        }

        List<Map.Entry<Integer, Course>> sortList = new ArrayList<>();
        for(Course course : allCourses){
            //compare the string of all the relevant attributes of the course to the query string
            sortList.add(new AbstractMap.SimpleEntry<>(FuzzySearch.tokenSetPartialRatio((course.department() + " " + course.name() + " " + course.professor() + " " + course.code()),searchQuery),course)); //TODO: change department to be a string of all the necessary parameters
        }
        //sort the list based on score
        sortList.sort((a,b)->Integer.compare(b.getKey(),a.getKey()));
        searchResults = sortList.stream().map(Map.Entry::getValue).collect(Collectors.toList());

        //applying filters will just need to take the searchresults returned by this code ^^ and only add the matching courses to the matchResults list
        matchResults = new ArrayList<>(searchResults);


    }

    public Search(String searchQuery, ArrayList<Filter> filterList) {
        //initialize the allCourses list if it's the first search
        if(allCourses == null){
            try {
                allCourses = loadData("courses.json");
            } catch (FileNotFoundException fnfe) {
                System.err.println("File not found: " + fnfe.getMessage());
            } catch (IOException ioe) {
                System.err.println("IO Exception occurred: " + ioe.getMessage());
            }
        }

    }

    public ArrayList<Filter> getFilters() {
        return null;
    }



    public static List<Course> loadData(String coursesFilename) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        URL jsonURL = Main.class.getResource(String.format("/%s", coursesFilename));
        if (jsonURL == null) {
            throw new FileNotFoundException(String.format("Could not find '%s' in resources directory", coursesFilename));
        }

        JsonNode root = mapper.readTree(jsonURL);
        JsonNode classesNode = root.get("classes"); // Grab the courses array inside the json

        return mapper.readerForListOf(Course.class).readValue(classesNode);
    }
}
