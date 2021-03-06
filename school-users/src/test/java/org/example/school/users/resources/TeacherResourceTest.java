package org.example.school.users.resources;

import com.google.gson.reflect.TypeToken;
import io.helidon.microprofile.tests.junit5.HelidonTest;
import org.example.school.users.protocol.teacher.GradeType;
import org.example.school.users.protocol.teacher.StudentStatus;
import org.example.school.users.protocol.teacher.TrackResItem;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.List;

@HelidonTest
class TeacherResourceTest extends CustomResourceTest {
    private static final String TEACHER_LOGIN = "naomi";
    private static final String TEACHER_PASSWORD = "gould";

    private enum TeacherMethods {
        TRACK(HttpMethod.GET), ADD_GRADES(HttpMethod.POST);

        public final HttpMethod method;

        TeacherMethods(HttpMethod method) {
            this.method = method;
        }

        public HttpMethod getMethod() {
            return method;
        }

        public MethodInfo getMethodInfo() {
            return new MethodInfo(toString(), method);
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase().replace('_', '-');
        }
    }

    @Override
    protected String getBasePath() {
        return "/teacher/";
    }

    @Override
    protected List<MethodInfo> getAllMethods() {
        return enumToSMethodInfo(TeacherMethods.class, TeacherMethods::getMethod);
    }

    @Test
    public void testWrongRoleMethods(WebTarget target) {
        testWrongRoleMethods(target, "admin", "adm");
        testWrongRoleMethods(target, "yousef", "chapman");
    }

    @Test
    void track(WebTarget target) throws IOException {
        target = target
            .queryParam("class", "5A")
            .queryParam("student", "ellis")
            .queryParam("dateFrom", "2022-01-02")
            .queryParam("dateTo", "2022-01-02");
        testOKMethod(
            target,
            TeacherMethods.TRACK.getMethodInfo(),
            TEACHER_LOGIN,
            TEACHER_PASSWORD,
            null,
            reader -> {
                List<TrackResItem> items = getGson().fromJson(reader, new TypeToken<List<TrackResItem>>() {}.getType());
                MatcherAssert.assertThat(items, Matchers.hasSize(1));
                TrackResItem expected = new TrackResItem(LocalDate.of(2022, 1, 2));
                expected.addGrade("Ellis Goff", 3, GradeType.EXAM);
                MatcherAssert.assertThat(items.get(0), Matchers.is(expected));
            });
    }

    @Test
    void trackWithoutClass(WebTarget target) {
        testFailedMethod(
            target,
            TeacherMethods.TRACK.getMethodInfo(),
            TEACHER_LOGIN,
            TEACHER_PASSWORD,
            null,
            Response.Status.BAD_REQUEST
        );
    }

    @Test
    void addGrades(WebTarget target) throws IOException {
        String body =
            "{\"class\":\"6B\",\"subject\":\"Chemistry\",\"date\":\"2022-01-01\",\"gradeType\":\"current\",\"grades\":[{\"student\":\"yousef\",\"grade\":3}]}";
        testOKMethod(
            target,
            TeacherMethods.ADD_GRADES.getMethodInfo(),
            "teacher",
            "teach",
            body,
            reader -> {
                StringWriter writer = new StringWriter();
                try {
                    reader.transferTo(writer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String str = writer.toString();
                List<StudentStatus> res = getGson().fromJson(str, new TypeToken<List<StudentStatus>>(){}.getType());
//                List<StudentStatus> res = getGson().fromJson(reader, new TypeToken<List<StudentStatus>>(){}.getType());
                MatcherAssert.assertThat(str, res, Matchers.hasSize(1));
                MatcherAssert.assertThat(res.get(0), Matchers.is(new StudentStatus("yousef", "OK")));
            }
        );
    }

}