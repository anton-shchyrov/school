package org.example.school.config;

import io.helidon.common.reactive.Single;
import io.helidon.config.Config;
import io.helidon.webserver.Routing;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.jersey.JerseySupport;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class Loader {
    private static JerseySupport buildJersey() {
        return JerseySupport.builder()
            .register(ConfigService.class)
            .register((ExceptionMapper<Exception>)exception -> {
                exception.printStackTrace();
                return Response.serverError().build();
            })
            .build();
    }

    public static void main(String[] args) {
        Config config = Config.create().get("server.port");
        startServer(config.asInt().orElse(7000));
    }

    public static Single<WebServer> startServer(int port) {
        Routing.Builder routing = Routing.builder()
            .register("/", buildJersey());
        return WebServer.builder().port(port).routing(routing).build().start();
    }
}
