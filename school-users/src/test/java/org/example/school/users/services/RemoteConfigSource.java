package org.example.school.users.services;

import org.eclipse.microprofile.config.spi.ConfigSource;

import java.util.Map;

public class RemoteConfigSource implements ConfigSource {
    private final Map<String, String> properties = Map.of();

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
