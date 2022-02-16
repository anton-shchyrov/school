package org.example.school.users.protocol;

import java.util.HashMap;
import java.util.Map;

public class QueryData {
    protected static void checkNotNull(Object value, String name) {
        if (value == null)
            throw new IllegalArgumentException(String.format("Field: \"%s\" is null", name));
    }

    protected static void checkNotEmptyString(String value, String name) {
        checkNotNull(value, name);
        if (value.isEmpty())
            throw new IllegalArgumentException(String.format("Field: \"%s\" is empty", name));
    }

    public static boolean testNotEmptyString(String value) {
        return (value != null) && !value.isEmpty();
    }

    public void validate() {}

    public Map<String, Object> createParams() {
        return new HashMap<>();
    }
}
