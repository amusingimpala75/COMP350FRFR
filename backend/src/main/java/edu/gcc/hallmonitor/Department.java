package edu.gcc.hallmonitor;

public record Department(String department) implements Filter {

    @Override
    public boolean filter(Course course) {
        return course.department().equals(department);
    }
}