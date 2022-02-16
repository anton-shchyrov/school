package org.example.school.users.protocol.teacher;

public class GradeItem {
    public final int grade;
    public final GradeType gradeType;

    public GradeItem(int grade, GradeType gradeType) {
        this.grade = grade;
        this.gradeType = gradeType;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof GradeItem))
            return false;
        GradeItem cmpItem = (GradeItem)obj;
        return (this.grade == cmpItem.grade) && (this.gradeType == cmpItem.gradeType);
    }
}
