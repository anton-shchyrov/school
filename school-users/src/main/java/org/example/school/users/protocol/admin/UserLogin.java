package org.example.school.users.protocol.admin;

import org.example.school.users.protocol.QueryData;

import java.util.Map;

public class UserLogin extends QueryData {
    public final String login;

    public UserLogin(String login) {
        this.login = login;
    }

    @Override
    public void validate() {
        super.validate();
        checkNotEmptyString(login, "login");
    }

    @Override
    public Map<String, Object> createParams() {
        Map<String, Object> res = super.createParams();
        res.put("login", login);
        return res;
    }
}
