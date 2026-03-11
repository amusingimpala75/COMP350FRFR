package edu.gcc.hallmonitor;

public class NumCredits extends Filter {

    public NumCredits(int credits) {
        this.numCredits = credits;
    }

    private int numCredits;

    @Override
    public boolean filter(Course course) {
        return course.credits() == numCredits;
    }
}
