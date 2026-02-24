package edu.gcc.hallmonitor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Search {

    private String searchQuery;
    private ArrayList<Filter> filterList;
    private ArrayList<Course> searchResults;
    private ArrayList<Course> matchResults;
    private static List<Course> allCourses; //intialized on the first search, shared across all searches. or constructor could call an init method

    public void applyFilter(Filter filter) {

    }

    public void removeFilter(Filter filter) {

    }

    public ArrayList<Course> getMatchResults() {
        //TODO: make a /results route and use this method to convert into json
        return matchResults;
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


        //TODO: parse query, get terms

        //TODO: order (add to) searchResults based on what most closely matches the terms

        //TODO: matchResults = searchResults since there are no filters
        //matchResults = searchResults;

        //for now:
        matchResults = new ArrayList<>(allCourses);


    }

    public Search(String searchQuery, ArrayList<Filter> filterList) {
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
