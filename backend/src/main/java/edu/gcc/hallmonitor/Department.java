package edu.gcc.hallmonitor;

public class Department extends Filter {

    public Department(String dept) {
        this.department = dept;
    }

    private String department;

    @Override
    public boolean filter(Course course) {
        return course.department().equals(department);
    }
}
