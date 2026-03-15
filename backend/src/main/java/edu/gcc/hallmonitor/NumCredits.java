package edu.gcc.hallmonitor;

public record NumCredits(int numCredits) implements Filter {

    @Override
    public boolean filter(Course course) {
        return course.credits() == numCredits;
    }
}