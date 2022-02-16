package org.example.school.users.protocol;

import com.google.gson.annotations.SerializedName;

public class Status {
    public enum StatusEnum {
        OK,
        @SerializedName("Not found")
        NOT_FOUND
    }

    public static final Status OK = new Status(StatusEnum.OK);
    public static final Status NOT_FOUND = new Status(StatusEnum.NOT_FOUND);

    public final StatusEnum status;

    private Status(StatusEnum status) {
        this.status = status;
    }
}
