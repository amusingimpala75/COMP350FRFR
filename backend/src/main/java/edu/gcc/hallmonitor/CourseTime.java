package edu.gcc.hallmonitor;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public record CourseTime(
    String day,
    @JsonProperty("start_time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    LocalTime startTime,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    @JsonProperty("end_time")
    LocalTime endTime
) {
}
