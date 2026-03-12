package edu.gcc.hallmonitor;

public class ProfName extends Filter {

    public ProfName(String profName) {
        this.profName = profName;
    }

    private String profName;

    @Override
    public boolean filter(Course course) {
        return course.professor().contains(profName);
    }

}