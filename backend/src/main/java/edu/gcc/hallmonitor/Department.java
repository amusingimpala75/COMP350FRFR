package edu.gcc.hallmonitor;

public class Department extends Filter {

    private String department;

    @Override
    public boolean filter(Course course) {
        return false;
    }
}
