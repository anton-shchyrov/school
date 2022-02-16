package org.example.school.protocol;

import java.util.Map;

public class Config {
    public final String database;
    public final Map<String, ServiceConfig> services;

    public Config() {
        database = null;
        services = null;
    }
}
