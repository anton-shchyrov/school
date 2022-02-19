package org.example.school.users.services;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.example.school.protocol.ServiceConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class RemoteConfigSource implements ConfigSource {
    private final Map<String, String> properties = new HashMap<>();

    public RemoteConfigSource() throws ExecutionException, InterruptedException {
        ServiceConfig cfg = RemoteConfigLoader.loadConfig("users");
        properties.put("server.port", Integer.toString(cfg.port));
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public String getValue(String propertyName) {
        return properties.get(propertyName);
    }

    @Override
    public String getName() {
        return "remote-properties";
    }
}
