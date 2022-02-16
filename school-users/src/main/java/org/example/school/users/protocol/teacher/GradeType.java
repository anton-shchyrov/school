package org.example.school.users.protocol.teacher;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

public enum GradeType {
    @SerializedName("current")
    CURRENT,
    @SerializedName("exam")
    EXAM;

    public static GradeType fromInteger(Integer val) {
        return (val == null) ?
            CURRENT :
            Arrays.stream(values()).filter(t -> t.ordinal() == val).findFirst().orElseThrow();
    }

    public Integer toInteger() {
        return (this == CURRENT) ? null : ordinal();
    }

    public static Integer toInteger(GradeType gradeType) {
        return (gradeType == null) ? null : gradeType.toInteger();
    }
}
