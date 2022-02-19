package org.example.school.users.resources;

import com.google.gson.reflect.TypeToken;
import org.example.school.users.protocol.student.StudentTrackItem;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.WebTarget;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

class StudentResourceTest extends CustomResourceTest {
    private static final MethodInfo TRACK_METHOD = new MethodInfo("track", HttpMethod.GET);

    @Override
    protected String getBasePath() {
        return "/student/";
    }

    @Override
    protected List<MethodInfo> getAllMethods() {
        return Collections.singletonList(TRACK_METHOD);
    }

    @Test
    public void testWrongRoleMethods() {
        testWrongRoleMethods("admin", "adm");
        testWrongRoleMethods("teacher", "teach");
    }

    @Test
    void track() throws IOException {
        WebTarget target = getAuthClient().target(getBaseUri())
            .queryParam("subject", "Chemistry")
            .queryParam("dateFrom", "2022-01-01")
            .queryParam("dateTo", "2022-01-01");
        testOKMethod(
            target,
            TRACK_METHOD,
            "yousef",
            "chapman",
            null,
            reader -> {
                List<StudentTrackItem> items = getGson().fromJson(reader, new TypeToken<List<StudentTrackItem>>() {}.getType());
                MatcherAssert.assertThat(items, Matchers.hasSize(1));
                StudentTrackItem item = items.get(0);
                MatcherAssert.assertThat(item.subject, Matchers.is("Chemistry"));
                MatcherAssert.assertThat(item.teacher, Matchers.is("Teacher"));
                MatcherAssert.assertThat(item.grades.size(), Matchers.greaterThan(0));
            });
    }
}