package org.example.school.users.services;

import io.helidon.config.Config;
import io.helidon.security.providers.httpauth.SecureUserStore;
import io.helidon.security.providers.httpauth.spi.UserStoreService;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Optional;

class DBUserStoreServiceTest {
    private static SecureUserStore store;

    @BeforeAll
    static void createDbClient() {
        UserStoreService service = new DBUserStoreService();
        store = service.create(null);
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