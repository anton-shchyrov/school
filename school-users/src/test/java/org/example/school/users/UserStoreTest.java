package org.example.school.users;

import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;
import io.helidon.security.providers.httpauth.SecureUserStore;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Optional;

class UserStoreTest {
    private static UserStore store;

    @BeforeAll
    static void createDbClient() {
        DbClient dbClient = DbClient.builder(Config.create().get("db")).build();
        store = new UserStore(dbClient);
    }

    @Test
    void userPassed() {
        Optional<SecureUserStore.User> user = store.user("admin");
        MatcherAssert.assertThat(user.isPresent(), Matchers.is(true));
        Collection<String> roles = user.get().roles();
        MatcherAssert.assertThat(roles, Matchers.contains("admin"));
    }

    @Test
    void userDenied() {
        Optional<SecureUserStore.User> user = store.user("qwerty");
        MatcherAssert.assertThat(user.isEmpty(), Matchers.is(true));
    }
}