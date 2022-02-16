package org.example.school.users;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collection;

class UserAuthTest {
    private static UserAuth user;

    @BeforeAll
    static void createUser() {
        user = new UserAuth("admin", "adm", UserRole.STAFF);
    }

    @Test
    void roles() {
        Collection<String> roles = user.roles();
        MatcherAssert.assertThat(roles, Matchers.hasSize(1));
        MatcherAssert.assertThat(roles, Matchers.hasItem(UserRole.STAFF.toString()));
    }

    @Test
    void isPasswordValid() {
        MatcherAssert.assertThat(user.isPasswordValid("adm".toCharArray()), Matchers.is(true));
    }

    @Test
    void isPasswordNotValid() {
        MatcherAssert.assertThat(user.isPasswordValid("adm1".toCharArray()), Matchers.is(false));
    }
}