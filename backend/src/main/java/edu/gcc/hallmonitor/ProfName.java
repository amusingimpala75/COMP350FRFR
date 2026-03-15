package edu.gcc.hallmonitor;

public record ProfName(String profName) implements Filter {

    @Override
    public boolean filter(Course course) {
        return course.professor().contains(profName);
    }

}