package org.example.school.users;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.school.users.protocol.ErrorMessage;

import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.time.format.DateTimeParseException;

public class ExceptionHandler {
    private static final Gson GSON = new GsonBuilder().create();

    private ExceptionHandler() {}

    private static Throwable getOriginalCause(Throwable throwable) {
        Throwable cause = throwable.getCause();
        while (cause != null) {
            throwable = cause;
            cause = throwable.getCause();
        }
        return throwable;
    }

    private static Response.Status decodeThrowable(Throwable throwable) {
        if (throwable instanceof IllegalArgumentException)
            return Response.Status.BAD_REQUEST;
        else if (throwable instanceof InterruptedException)
            return Response.Status.SERVICE_UNAVAILABLE;
        else if (throwable instanceof SQLException)
            return Response.Status.BAD_REQUEST;
        else if (throwable instanceof DateTimeParseException)
            return Response.Status.BAD_REQUEST;
        else
            return Response.Status.INTERNAL_SERVER_ERROR;
    }

    public static Response.ResponseBuilder handle(Throwable throwable) {
        throwable = getOriginalCause(throwable);
        Response.Status status = decodeThrowable(throwable);
        return Response.status(status).entity(GSON.toJson(new ErrorMessage(throwable)));
    }
}
