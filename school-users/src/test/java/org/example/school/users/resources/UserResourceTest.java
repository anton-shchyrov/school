package org.example.school.users.resources;

import org.example.school.users.UserRole;
import org.example.school.users.protocol.*;
import org.example.school.users.protocol.admin.UserAdd;
import org.example.school.users.protocol.admin.UserLogin;
import org.example.school.users.protocol.admin.UserModify;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

class UserResourceTest extends CustomResourceTest {
    private enum UserMethods {
        ADD, UPDATE, DELETE;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    private static final String ADMIN_LOGIN = "admin";
    private static final String ADMIN_PASSWORD = "adm";
    private static final String USER_LOGIN = "test-user";
    private static final String NOT_EXISTS_LOGIN = "not-exists-user";

    @Override
    protected String getBasePath() {
        return "/users/";
    }

    @Override
    protected List<MethodInfo> getAllMethods() {
        return enumToSMethodInfo(UserMethods.class, name-> HttpMethod.POST);
    }

    private void testUserMethod(WebTarget target, UserMethods method, String body) throws IOException {
        testUserMethod(target, method, body, Status.StatusEnum.OK);
    }

    private void testUserMethod(WebTarget target, UserMethods method, String body, Status.StatusEnum waitStatus) throws IOException {
        testOKMethod(
            target,
            new MethodInfo(method.toString(), HttpMethod.POST),
            ADMIN_LOGIN,
            ADMIN_PASSWORD,
            body,
            reader -> {
                Status status = getGson().fromJson(reader, Status.class);
                MatcherAssert.assertThat(status.status, Matchers.notNullValue());
                if (waitStatus != null)
                    MatcherAssert.assertThat(status.status, Matchers.is(waitStatus));
            }
        );
    }

    @Test
    public void testWrongRoleMethods() {
        testWrongRoleMethods("teacher", "teach");
        testWrongRoleMethods("yousef", "chapman");
    }

    @Test
    public void testUsers() throws IOException {
        UserAdd user = new UserAdd(USER_LOGIN, "123", "Test User", UserRole.TEACHER);
        WebTarget target = getAuthClient().target(getBaseUri());
        testUserMethod(target, UserMethods.DELETE, getGson().toJson(user, UserLogin.class), null);
        testUserMethod(target, UserMethods.ADD, getGson().toJson(user));
        testUserMethod(target, UserMethods.UPDATE, getGson().toJson(new UserModify(USER_LOGIN, null, "New name")));
        testUserMethod(target, UserMethods.DELETE, getGson().toJson(user, UserLogin.class));
    }

    @Test
    public void testNotExists() throws IOException {
        UserModify user = new UserModify(NOT_EXISTS_LOGIN, "new-pass", "Name");
        WebTarget target = getAuthClient().target(getBaseUri());
        testUserMethod(target, UserMethods.UPDATE, getGson().toJson(user), Status.StatusEnum.NOT_FOUND);
        testUserMethod(target, UserMethods.DELETE, getGson().toJson(user, UserLogin.class), Status.StatusEnum.NOT_FOUND);
    }

    @Test
    public void testEmptyUpdate() {
        UserModify user = new UserModify(ADMIN_LOGIN, null, null);
        WebTarget target = getAuthClient().target(getBaseUri());
        testFailedMethod(
            target,
            new MethodInfo(UserMethods.UPDATE.toString(), HttpMethod.POST),
            ADMIN_LOGIN,
            ADMIN_PASSWORD,
            getGson().toJson(user),
            Response.Status.BAD_REQUEST
        );
    }

}