package org.example.school.users.protocol.admin;

import java.util.Map;

public class UserModify extends UserLogin {
    public final String password;
    public final String name;

    public UserModify(String login, String password, String name) {
        super(login);
        this.password = password;
        this.name = name;
    }

    @Override
    public void validate() {
        super.validate();
        if (!testNotEmptyString(password) && !testNotEmptyString(name))
            throw new  IllegalArgumentException("\"password\" or \"name\" field must be specified");
    }

    @Override
    public Map<String, Object> createParams() {
        Map<String, Object> res = super.createParams();
        if (testNotEmptyString(password))
            res.put("password", password);
        if (testNotEmptyString(name))
            res.put("name", name);
        return res;
    }
}
