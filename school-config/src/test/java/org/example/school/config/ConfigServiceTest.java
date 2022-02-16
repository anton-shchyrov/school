package org.example.school.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.helidon.webserver.WebServer;
import org.example.school.protocol.Config;
import org.example.school.protocol.ServiceConfig;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class ConfigServiceTest {
    private static WebServer server;
    private static Client baseClient;
    private static Gson gson;

    @BeforeAll
    public static void init() throws InterruptedException, ExecutionException, TimeoutException {
        server = Loader.startServer(0).get(5, TimeUnit.SECONDS);
        baseClient = ClientBuilder.newClient();
        gson = new GsonBuilder().create();
    }

    @AfterAll
    public static void done() throws InterruptedException {
        baseClient.close();
        stopServer(server);
    }

    private static void stopServer(WebServer server) throws InterruptedException {
        if (null == server) {
            return;
        }
        CountDownLatch cdl = new CountDownLatch(1);
        long t = System.nanoTime();
        server.shutdown().thenAccept(webServer -> {
            long time = System.nanoTime() - t;
            System.out.println("Server shutdown in " + TimeUnit.NANOSECONDS.toMillis(time) + " ms");
            cdl.countDown();
        });

        if (!cdl.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Failed to shutdown server within 5 seconds");
        }
    }

    protected static String getBaseUri() {
        return String.format("http://localhost:%d/", server.port());
    }

    @Test
    public void testConfig() {
        WebTarget target = baseClient.target(getBaseUri());
        try (Response response = target
            .path("/config")
            .request(MediaType.APPLICATION_JSON)
            .get())
        {
            MatcherAssert.assertThat(response.getStatus(), Matchers.is(200));
            InputStream inputStream = response.readEntity(InputStream.class);
            Config config = gson.fromJson(new InputStreamReader(inputStream), Config.class);
            MatcherAssert.assertThat(config.database, Matchers.not(Matchers.isEmptyOrNullString()));
            MatcherAssert.assertThat(config.services, Matchers.notNullValue());
            MatcherAssert.assertThat(config.services, Matchers.hasKey("users"));
        }
    }

    @Test
    public void testServiceUsers() {
        WebTarget target = baseClient.target(getBaseUri());
        try (Response response = target
            .path("/config/services/users")
            .request(MediaType.APPLICATION_JSON)
            .get())
        {
            MatcherAssert.assertThat(response.getStatus(), Matchers.is(200));
            InputStream inputStream = response.readEntity(InputStream.class);
            ServiceConfig config = gson.fromJson(new InputStreamReader(inputStream), ServiceConfig.class);
            MatcherAssert.assertThat(config.address, Matchers.not(Matchers.isEmptyOrNullString()));
            MatcherAssert.assertThat(config.port, Matchers.greaterThan(0));
        }
    }

    @Test
    public void testServiceUnknown() {
        WebTarget target = baseClient.target(getBaseUri());
        try (Response response = target
            .path("/config/services/missed-service")
            .request(MediaType.APPLICATION_JSON)
            .get())
        {
            MatcherAssert.assertThat(response.getStatus(), Matchers.is(404));
        }
    }
}