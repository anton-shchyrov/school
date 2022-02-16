package org.example.school.users.protocol.student;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class StudentTrackItem {
    public final String subject;
    public final String teacher;
    public final List<GradeDateItem> grades;

    public StudentTrackItem(String subject, String teacher) {
        this.subject = subject;
        this.teacher = teacher;
        grades = new LinkedList<>();
    }

    public double getAverage() {
        return grades.stream()
            .filter(grade -> grade.grade > 0)
            .mapToDouble(grade -> grade.grade)
            .average()
            .orElseThrow();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof StudentTrackItem))
            return false;
        StudentTrackItem that = (StudentTrackItem)obj;
        return
            Objects.equals(subject, that.subject) &&
            Objects.equals(teacher, that.teacher) &&
            Objects.equals(grades, that.grades);
    }
}
