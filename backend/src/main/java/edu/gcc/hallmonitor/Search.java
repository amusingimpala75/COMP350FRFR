package edu.gcc.hallmonitor;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Search {

    private String searchQuery;
    private ArrayList<Filter> filterList;
    private ArrayList<Course> searchResults;
    private ArrayList<Course> matchResults;

    public void applyFilter(Filter filter) {

    }

    public void removeFilter(Filter filter) {

    }

    public ArrayList<Course> getMatchResults() {
        return null;
    }

    //The constructors will search the db for the appropriate courses
    public Search(String searchQuery) {

    }

    public Search(String searchQuery, ArrayList<Filter> filterList) {

    }

    public ArrayList<Filter> getFilters() {
        return null;
    }


}
