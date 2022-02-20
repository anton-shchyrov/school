package org.example.school.users.protocol;

import com.google.gson.annotations.SerializedName;

public class ErrorMessage {
    @SuppressWarnings("unused")
    @SerializedName("error")
    public final String message;

    public ErrorMessage(Throwable throwable) {
        this.message = throwable.getClass().getName() + ": " + throwable.getMessage();
    }
}
