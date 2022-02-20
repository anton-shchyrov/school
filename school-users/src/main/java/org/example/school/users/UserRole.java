package org.example.school.users;

import com.google.gson.annotations.SerializedName;

class UserRoleNames {
    public static final String ADMIN = "admin";
    public static final String TEACHER = "teacher";
    public static final String STUDENT = "student";
    public static final String STAFF = "staff";
    private UserRoleNames() {}
}

@SuppressWarnings("unused")
public enum UserRole {
    @SerializedName(UserRoleNames.ADMIN)
    ADMIN,
    @SerializedName(UserRoleNames.TEACHER)
    TEACHER,
    @SerializedName(UserRoleNames.STUDENT)
    STUDENT,
    @SerializedName(UserRoleNames.STAFF)
    STAFF;

    public static final String ADMIN_ROLE = UserRoleNames.ADMIN;
    public static final String TEACHER_ROLE = UserRoleNames.TEACHER;
    public static final String STUDENT_ROLE = UserRoleNames.STUDENT;
    public static final String STAFF_ROLE = UserRoleNames.STAFF;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }

    public static UserRole getRole(String name) {
        return valueOf(name.toUpperCase());
    }
}
