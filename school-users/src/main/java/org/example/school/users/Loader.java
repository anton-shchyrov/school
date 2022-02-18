package org.example.school.users;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.helidon.common.context.Contexts;
import io.helidon.common.reactive.Single;
import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;
import io.helidon.security.Security;
import io.helidon.security.integration.jersey.SecurityFeature;
import io.helidon.security.providers.abac.AbacProvider;
import io.helidon.security.providers.common.OutboundTarget;
import io.helidon.security.providers.httpauth.HttpBasicAuthProvider;
import io.helidon.webclient.WebClient;
import io.helidon.webserver.Routing;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.jersey.JerseySupport;
import org.example.school.protocol.ServiceConfig;
import org.example.school.users.services.StudentService;
import org.example.school.users.services.TeacherService;
import org.example.school.users.services.UserService;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.concurrent.ExecutionException;

public class Loader {
    private static SecurityFeature buildSecurity(DbClient dbClient) {
        return new SecurityFeature(
            Security.builder()
                .addProvider(HttpBasicAuthProvider.builder()
                    .realm("school-users")
                    .userStore(new UserStore(dbClient))
                    .addOutboundTarget(OutboundTarget.builder("propagate-all").build()))
                .addProvider(AbacProvider.create())
                .build());
    }

    private static JerseySupport buildJersey(Config config) {
        DbClient dbClient = DbClient.builder(config.get("db")).build();
        Contexts.globalContext().register(dbClient);
        return JerseySupport.builder()
            .register(UserService.class)
            .register(TeacherService.class)
            .register(StudentService.class)
            .register(buildSecurity(dbClient))
            .register((ExceptionMapper<Exception>) exception -> {
                exception.printStackTrace();
                return Response.serverError().build();
            })
            .build();
    }

    private static int getPort(Config config) throws ExecutionException, InterruptedException {
        WebClient client = WebClient.builder()
            .baseUri(config.get("services.config")
                .asString()
                .orElseThrow(() -> new RuntimeException("Config key \"services.config\" not found"))
            )
            .build();

        Single<String> response = client.get()
            .path("/config/services/users")
            .request(String.class);
        Gson gson = new GsonBuilder().create();
        ServiceConfig cfg = gson.fromJson(response.get(), ServiceConfig.class);
        return cfg.port;
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Config config = Config.create();
        int port = getPort(config);
        startServer(config, port);
    }

    public static Single<WebServer> startServer(Config config, int port) {
        Routing.Builder routing = Routing.builder()
            .register("/", buildJersey(config));
        return WebServer.builder().port(port).routing(routing).build().start();
    }
}
