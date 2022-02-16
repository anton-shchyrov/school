package org.example.school.users.protocol.teacher;

import java.util.Objects;

public class StudentStatus {
    public final String student;
    public final String status;

    public StudentStatus(String student, String status) {
        this.student = student;
        this.status = status;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof StudentStatus))
            return false;
        StudentStatus that = (StudentStatus)obj;
        return Objects.equals(student, that.student) && Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(student, status);
    }
}
