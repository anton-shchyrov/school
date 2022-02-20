package org.example.school.users.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.helidon.common.reactive.Single;
import io.helidon.config.Config;
import io.helidon.webclient.WebClient;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.example.school.protocol.ServiceConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class RemoteConfigSource implements ConfigSource {
    private final Map<String, String> properties = new HashMap<>();

    public RemoteConfigSource() throws ExecutionException, InterruptedException {
        ServiceConfig cfg = loadConfig();
        properties.put("server.port", Integer.toString(cfg.port));
    }

    private static ServiceConfig loadConfig() throws ExecutionException, InterruptedException {
        WebClient client = WebClient.builder()
            .baseUri(Config.create().get("services.config")
                .asString()
                .orElseThrow(() -> new RuntimeException("Config key \"services.config\" not found"))
            )
            .build();
        Single<String> response = client.get()
            .path("/config/services/users")
            .request(String.class);
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(response.get(), ServiceConfig.class);
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
