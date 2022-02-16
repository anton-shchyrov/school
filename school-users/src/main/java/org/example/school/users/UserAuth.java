package org.example.school.users;

import io.helidon.security.providers.httpauth.SecureUserStore;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

class UserAuth implements SecureUserStore.User {
    private final String login;
    private final String password;
    private final UserRole role;
    private final LocalDateTime ttl;

    UserAuth(String login, String password, UserRole role) {
        this.login = login;
        this.password = password;
        this.role = role;
        this.ttl = LocalDateTime.now().plusHours(1);
    }

    @Override
    public Collection<String> roles() {
        return List.of(role.toString());
    }

    @Override
    public String login() {
        return login;
    }

    @Override
    public boolean isPasswordValid(char[] password) {
        return Arrays.equals(password, this.password.toCharArray());
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(ttl);
    }
}
