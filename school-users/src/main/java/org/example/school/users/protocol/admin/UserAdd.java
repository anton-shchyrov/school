package org.example.school.users.protocol.admin;

import org.example.school.users.UserRole;

import java.util.Map;

public class UserAdd extends UserModify {
    public final UserRole role;

    public UserAdd(String login, String password, String name, UserRole role) {
        super(login, password, name);
        this.role = role;
    }


    @Override
    public void validate() {
        checkNotEmptyString(password, "password");
        checkNotEmptyString(name, "name");
        checkNotNull(role, "role");
        super.validate();
    }

    @Override
    public Map<String, Object> createParams() {
        Map<String, Object> res = super.createParams();
        res.put("role", role.ordinal());
        return res;
    }
}
