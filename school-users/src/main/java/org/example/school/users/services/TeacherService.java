package org.example.school.users.services;

import io.helidon.security.SecurityContext;
import io.helidon.security.annotations.Authenticated;
import io.helidon.security.annotations.Authorized;
import org.example.school.users.ExceptionHandler;
import org.example.school.users.UserRole;
import org.example.school.users.protocol.teacher.GradeType;
import org.example.school.users.protocol.teacher.SetGrades;
import org.example.school.users.protocol.teacher.StudentStatus;
import org.example.school.users.protocol.teacher.TrackResItem;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

@Path("/teacher")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
@Authorized
@RolesAllowed(UserRole.TEACHER_ROLE)
public class TeacherService extends CustomService {

    private static Map<String, Object> trackParams(
        String teacher,
        String className,
        String studentLogin,
        String dateFromStr,
        String dateToStr
    ) {
        Map<String, Object> res = new HashMap<>();
        res.put("teacher", teacher);
        res.put("class", className);
        if (studentLogin != null && !studentLogin.isEmpty())
            res.put("student", studentLogin);
        if (dateFromStr != null)
            res.put("date_from", LocalDate.parse(dateFromStr, DateTimeFormatter.ISO_DATE));
        if (dateToStr != null)
            res.put("date_to", LocalDate.parse(dateToStr, DateTimeFormatter.ISO_DATE));
        return res;
    }

    @GET
    @Path("track")
    public Response track(
        @Context SecurityContext context,
        @QueryParam("class") String className,
        @QueryParam("student") String studentLogin,
        @QueryParam("dateFrom") String dateFromStr,
        @QueryParam("dateTo") String dateToStr
    ) {
        Response.ResponseBuilder resp;
        try {
            if (className == null || className.isEmpty())
                throw new IllegalArgumentException("Class must be specified");
            StringBuilder builder = new StringBuilder(getStatement("journal-get"));
            if (studentLogin != null && !studentLogin.isEmpty())
                builder.append(" AND jrn.\"STUDENT_LOGIN\" = :student");
            if (dateFromStr != null)
                builder.append(" AND jrn.\"DATE\" >= :date_from");
            if (dateToStr != null)
                builder.append(" AND jrn.\"DATE\" <= :date_to");
            builder.append(" ORDER BY jrn.\"DATE\", jrn.\"STUDENT_NAME\"");
            List<TrackResItem> res = getDbClient().execute(exec -> exec
                .createQuery(builder.toString())
                .params(
                    trackParams(
                        context.userName(),
                        className,
                        studentLogin,
                        dateFromStr,
                        dateToStr
                    )
                )
                .execute()
                .reduce(
                    LinkedList<TrackResItem>::new,
                    (list, row) -> {
                        LocalDate curDate = row.column("DATE").as(LocalDate.class);
                        TrackResItem lastItem = list.peekLast();
                        LocalDate lastDate = (lastItem != null) ? lastItem.date : null;
                        if (!curDate.equals(lastDate)) {
                            lastItem = new TrackResItem(curDate);
                            list.addLast(lastItem);
                        }
                        lastItem.addGrade(
                            row.column("STUDENT_NAME").as(String.class),
                            row.column("GRADE").as(Integer.class),
                            GradeType.fromInteger(row.column("GRADE_TYPE").as(Integer.class))
                        );
                        return list;
                    })
            ).get();
            resp = Response.ok(GSON.toJson(res));
        } catch (Throwable t) {
            resp = ExceptionHandler.handle(t);
        }
        return resp.build();
    }

    @POST
    @Path("add-grades")
    public void addGrades(
        @Context SecurityContext context,
        @Suspended AsyncResponse response,
        String data
    ) {
        try {
            SetGrades grades = GSON.fromJson(data, SetGrades.class);
            grades.validate();
            getDbClient().execute(exec -> exec
                .createNamedGet("class-subj-id")
                .addParam("teacher", context.userName())
                .addParam("class", grades.className)
                .addParam("subject", grades.subject)
                .execute()
            ).thenAccept(csRow -> {
                int csId = csRow.orElseThrow().column("ID").as(Integer.class);
                Integer gradeType = GradeType.toInteger(grades.gradeType);
                CountDownLatch counter = new CountDownLatch(grades.getGrades().size());
                Queue<StudentStatus> statuses = new ConcurrentLinkedQueue<>();
                grades.getGrades().forEach(grade ->
                    getDbClient().execute(exec -> exec
                        .createNamedGet("student-id")
                        .addParam("student", grade.student)
                        .execute()
                    ).thenAccept(stdRow -> {
                        int stdId = stdRow.orElseThrow().column("ID").as(Integer.class);
                        getDbClient().execute(exec -> exec
                            .createNamedInsert("journal-add")
                            .addParam("student_id", stdId)
                            .addParam("class_subj_id", csId)
                            .addParam("date", grades.date)
                            .addParam("grade", grade.grade)
                            .addParam("grade_type", gradeType)
                            .execute()
                        ).whenComplete((cnt, t) -> {
                            String status = (t != null) ? t.getMessage() : "OK";
                            statuses.add(new StudentStatus(grade.student, status));
                            counter.countDown();
                        });
                    }).exceptionally(t -> {
                        statuses.add(new StudentStatus(grade.student, t.getMessage()));
                        counter.countDown();
                        return null;
                    })
                );
                new Thread(() -> {
                    try {
                        counter.await();
                        response.resume(GSON.toJson(statuses));
                    } catch (InterruptedException e) {
                        sendError(e, response);
                    }
                }).start();
            }).exceptionally(
                t -> sendError(t, response)
            );
        } catch (Throwable t) {
            sendError(t, response);
        }
    }

    @SuppressWarnings("SameReturnValue")
    private Void sendError(Throwable throwable, AsyncResponse response) {
        throwable.printStackTrace();
        response.resume(ExceptionHandler.handle(throwable).build());
        return null;
    }
}
