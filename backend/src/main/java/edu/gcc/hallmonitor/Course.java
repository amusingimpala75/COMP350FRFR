package edu.gcc.hallmonitor;

import java.util.ArrayList;

public record Course(
        String name,
        ArrayList<String> professor,
        String department,
        int code,
        char section,
        String location,
        int credits,
        String semester,
        ArrayList<String> times,
        boolean isLab,
        int numOpenSeats,
        int totalSeats
) {
// adding a change by Hudson
}
