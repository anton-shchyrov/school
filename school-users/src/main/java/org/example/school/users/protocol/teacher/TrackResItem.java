package org.example.school.users.protocol.teacher;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.*;

public class TrackResItem {

    public final LocalDate date;
    public final Map<String, List<GradeItem>> grades = new HashMap<>();

    public TrackResItem(LocalDate date) {
        this.date = date;
    }

    public void addGrade(String student, int grade, @NotNull GradeType gradeType) {
        grades.computeIfAbsent(
            student,
            s -> new LinkedList<>()
        ).add(new GradeItem(grade, gradeType));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof TrackResItem))
            return false;
        TrackResItem cmpObj = (TrackResItem)obj;
        return Objects.equals(this.date, cmpObj.date) && Objects.equals(this.grades, cmpObj.grades);
    }
}
