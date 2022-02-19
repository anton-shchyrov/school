package org.example.school.users.services;

import io.helidon.security.SecurityContext;
import io.helidon.security.annotations.Authenticated;
import io.helidon.security.annotations.Authorized;
import org.example.school.users.ExceptionHandler;
import org.example.school.users.UserRole;
import org.example.school.users.protocol.student.GradeDateItem;
import org.example.school.users.protocol.student.StudentTrackItem;
import org.example.school.users.protocol.teacher.GradeType;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@ApplicationScoped
@Path("/student")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
@Authorized
@RolesAllowed(UserRole.STUDENT_ROLE)
public class StudentService extends CustomService {

    private static Map<String, Object> trackParams(
        String student,
        String subject,
        String dateFromStr,
        String dateToStr
    ) {
        Map<String, Object> res = new HashMap<>();
        res.put("student", student);
        if (subject != null && !subject.isEmpty())
            res.put("subject", subject);
        if (dateFromStr != null)
            res.put("date_from", LocalDate.parse(dateFromStr, DateTimeFormatter.ISO_DATE));
        if (dateToStr != null)
            res.put("date_to", LocalDate.parse(dateToStr, DateTimeFormatter.ISO_DATE));
        return res;
    }

    @Inject
    public StudentService(@Named("school") DataSource dataSource) {
        super(dataSource);
    }

    @GET
    @Path("track")
    public Response track(
        @Context SecurityContext context,
        @QueryParam("subject") String subject,
        @QueryParam("dateFrom") String dateFromStr,
        @QueryParam("dateTo") String dateToStr
    ) {
        Response.ResponseBuilder resp;
        try {
            StringBuilder builder = new StringBuilder(getStatement("student-track"));
            if (subject != null && !subject.isEmpty())
                builder.append(" AND subj.\"NAME\" = :subject");
            if (dateFromStr != null)
                builder.append(" AND jrn.\"DATE\" >= :date_from");
            if (dateToStr != null)
                builder.append(" AND jrn.\"DATE\" <= :date_to");
            builder.append(" ORDER BY subj.\"NAME\", jrn.\"DATE\"");
            List<StudentTrackItem> res = getDbClient().execute(exec -> exec
                .createQuery(builder.toString())
                .params(
                    trackParams(
                        context.userName(),
                        subject,
                        dateFromStr,
                        dateToStr
                    )
                )
                .execute()
                .reduce(
                    LinkedList<StudentTrackItem>::new,
                    (list, row) -> {
                        String curSubject = row.column("SUBJECT").as(String.class);
                        StudentTrackItem lastItem = list.peekLast();
                        String lastSubject = (lastItem != null) ? lastItem.subject : null;
                        if (!curSubject.equals(lastSubject)) {
                            lastItem = new StudentTrackItem(
                                curSubject,
                                row.column("TEACHER").as(String.class)
                            );
                            list.addLast(lastItem);
                        }
                        lastItem.grades.add(new GradeDateItem(
                            row.column("GRADE").as(Integer.class),
                            GradeType.fromInteger(row.column("GRADE_TYPE").as(Integer.class)),
                            row.column("DATE").as(LocalDate.class)
                        ));
                        return list;
                    })
            ).get();
            resp = Response.ok(GSON.toJson(res));
        } catch (Throwable t) {
            resp = ExceptionHandler.handle(t);
        }
        return resp.build();
    }
}
