package org.example.school.users;

import io.helidon.common.reactive.Single;
import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.DbRow;
import io.helidon.security.providers.httpauth.SecureUserStore;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class UserStore implements SecureUserStore {
    private final DbClient dbClient;
    private final Map<String, UserAuth> users;

    public UserStore(DbClient dbClient) {
        this.dbClient = dbClient;
        users = new ConcurrentHashMap<>();
    }

    @Override
    public Optional<User> user(String login) {
        UserAuth user = users.computeIfPresent(login, (name, info) -> info.isExpired() ? null : info);
        if (user == null) {
            try {
                user = readUser(login);
                if (user != null)
                    users.putIfAbsent(login, user);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return Optional.ofNullable(user);
    }

    private UserAuth readUser(String login) throws ExecutionException, InterruptedException {
        Single<Optional<DbRow>> rowSingle = dbClient.execute(exec ->
            exec.createNamedGet("user-auth")
            .addParam("login", login)
            .execute()
        );
        Optional<UserAuth> res = rowSingle.get().map(row -> {
            String rLogin = row.column("LOGIN").as(String.class);
            String rPassword = row.column("PASSWORD").as(String.class);
            String rRole = row.column("ROLE").as(String.class);
            return new UserAuth(rLogin, rPassword, UserRole.getRole(rRole));
        });
        return res.orElse(null);
    }
}
