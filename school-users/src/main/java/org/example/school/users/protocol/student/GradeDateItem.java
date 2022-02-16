package org.example.school.users.protocol.student;

import org.example.school.users.protocol.teacher.GradeItem;
import org.example.school.users.protocol.teacher.GradeType;

import java.time.LocalDate;

public class GradeDateItem extends GradeItem {
    public final LocalDate date;

    public GradeDateItem(int grade, GradeType gradeType, LocalDate date) {
        super(grade, gradeType);
        this.date = date;
    }
}
