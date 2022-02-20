package org.example.school.users.protocol.teacher;

import com.google.gson.annotations.SerializedName;
import org.example.school.users.protocol.QueryData;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class SetGrades extends QueryData {
    public static class StudentGrade {
        public final String student;
        public final Integer grade;

        private StudentGrade() {
            student = null;
            grade = null;
        }

        public void validate() {
            checkNotEmptyString(student, "student");
            checkNotNull(grade, "grade");
        }
    }
    @SerializedName("class")
    public final String className;
    public final String subject;
    public final LocalDate date;
    public final GradeType gradeType;
    private List<StudentGrade> grades;

    public SetGrades() {
        className = null;
        subject = null;
        date = null;
        gradeType = null;
        grades = null;
    }

    public List<StudentGrade> getGrades() {
        return grades;
    }

    @Override
    public void validate() {
        super.validate();
        checkNotEmptyString(className, "class");
        checkNotEmptyString(subject, "subject");
        checkNotNull(date, "date");
        checkNotNull(grades, "grades");
        grades = grades.stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (grades.isEmpty())
            throw new IllegalArgumentException("Grades list is empty");
        grades.forEach(StudentGrade::validate);
    }

    @Override
    public Map<String, Object> createParams() {
        Map<String, Object> res = super.createParams();
        res.put("date", date);
        res.put("grade_type", (gradeType == GradeType.CURRENT) ? null : gradeType);
        return res;
    }

}
