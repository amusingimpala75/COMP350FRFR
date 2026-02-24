package edu.gcc.hallmonitor;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Map;

public record Course(
        String name,
        @JsonProperty("faculty")
        ArrayList<String> professor,
        @JsonProperty("subject")
        String department,
        @JsonProperty("number")
        int code,
        char section,
        String location,
        int credits,
        String semester,
        ArrayList<Map<String, String>> times,
        @JsonProperty("is_lab")
        boolean isLab,
        @JsonProperty("is_open")
        boolean isOpen,
        @JsonProperty("open_seats")
        int numOpenSeats,
        @JsonProperty("total_seats")
        int totalSeats
) {}
