package edu.gcc.hallmonitor;

import java.util.function.Predicate;


public interface Filter extends Predicate<Course> {
    boolean filter(Course c);

    default boolean test(Course c) {
        return filter(c);
    }

}
