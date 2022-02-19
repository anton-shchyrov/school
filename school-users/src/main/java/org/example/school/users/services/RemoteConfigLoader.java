package org.example.school.users.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.helidon.common.reactive.Single;
import io.helidon.config.Config;
import io.helidon.webclient.WebClient;
import org.example.school.protocol.ServiceConfig;

import java.util.concurrent.ExecutionException;

class RemoteConfigLoader {
    private static final WebClient client = WebClient.builder()
        .baseUri(Config.create().get("services.config")
            .asString()
            .orElseThrow(() -> new RuntimeException("Config key \"services.config\" not found"))
        )
        .build();

    public static ServiceConfig loadConfig(String serviceName) throws ExecutionException, InterruptedException {
        Single<String> response = client.get()
            .path("/config/services/" + serviceName)
            .request(String.class);
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(response.get(), ServiceConfig.class);
    }
}
