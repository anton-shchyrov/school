package org.example.school.users.resources;

import com.google.gson.Gson;
import io.helidon.config.Config;
import io.helidon.webserver.WebServer;
import org.example.school.users.Loader;
import org.example.school.users.protocol.Status;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

abstract class CustomResourceTest {
    protected enum HttpMethod {
        GET, POST
    }

    protected static class MethodInfo {
        public final String name;
        public final HttpMethod method;

        public MethodInfo(String name, HttpMethod method) {
            this.name = name;
            this.method = method;
        }
    }

    protected static final String AUTH_USER_NAME_PARAM = HttpAuthenticationFeature.HTTP_AUTHENTICATION_USERNAME;
    protected static final String AUTH_PASSWORD_PARAM = HttpAuthenticationFeature.HTTP_AUTHENTICATION_PASSWORD;

    private static WebServer server;
    private static Client baseClient;
    private static Client authClient;

    private static Gson gson;

    public static Client getAuthClient() {
        return authClient;
    }

    public static Gson getGson() {
        return gson;
    }

    @BeforeAll
    public static void init() throws ExecutionException, InterruptedException, TimeoutException {
        server = Loader.startServer(Config.create(), 0).get(5, TimeUnit.SECONDS);
        gson = CustomResource.GSON;

        baseClient = ClientBuilder.newClient();
        authClient = ClientBuilder.newClient();
        HttpAuthenticationFeature feature = HttpAuthenticationFeature.basicBuilder().nonPreemptive().build();
        authClient.register(feature);
    }

    @AfterAll
    public static void done() throws InterruptedException {
        baseClient.close();
        authClient.close();
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

    protected abstract String getBasePath();
    protected abstract List<MethodInfo> getAllMethods();

    protected static <T extends Enum<T>> List<MethodInfo> enumToSMethodInfo(
        Class<T> clazz, Function<T, HttpMethod> resolver) {
        return Arrays.stream(clazz.getEnumConstants())
            .map(val -> new MethodInfo(val.toString(), resolver.apply(val)))
            .collect(Collectors.toList());
    }

    private static <T> Entity<T> getEntity(HttpMethod method, T data) {
        return (method == HttpMethod.POST && data != null) ? Entity.json(data) : null;
    }

    protected void testMethod(
        WebTarget target,
        MethodInfo method,
        String login,
        String password,
        String body,
        Response.Status expectedStatus,
        Consumer<Reader> validator
    ) throws IOException {
        try (Response response = target
            .path(getBasePath() + method.name)
            .request(MediaType.APPLICATION_JSON)
            .property(AUTH_USER_NAME_PARAM, login)
            .property(AUTH_PASSWORD_PARAM, password)
            .method(method.method.toString(), getEntity(method.method, body))
        ) {
            MatcherAssert.assertThat(response.getStatus(), Matchers.is(expectedStatus.getStatusCode()));
            if (validator != null) {
                try (
                    InputStream inputStream = response.readEntity(InputStream.class);
                    Reader reader = new InputStreamReader(inputStream)
                ) {
                    validator.accept(reader);
                }
            }
        }
    }

    protected void testOKMethod(
        WebTarget target,
        MethodInfo method,
        String login,
        String password,
        String body,
        Consumer<Reader> validator
    ) throws IOException {
        testMethod(
            target,
            method,
            login,
            password,
            body,
            Response.Status.OK,
            validator
        );
    }

    protected void testFailedMethod(
        WebTarget target,
        MethodInfo method,
        String login,
        String password,
        String body,
        Response.Status expectedStatus
    ) {
        try {
            testMethod(
                target,
                method,
                login,
                password,
                body,
                expectedStatus,
                null
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void testUnprotected(WebTarget target, MethodInfo method) {
        try (Response response = target
            .path(getBasePath() + method.name)
            .request(MediaType.APPLICATION_JSON)
            .method(method.method.toString(), getEntity(method.method, gson.toJson(Status.OK)))
        ) {
            MatcherAssert.assertThat(
                String.format("Failed unprotected access for method \"%s\"", method),
                response.getStatus(),
                Matchers.is(401)
            );
        }
    }

    private void testWrongPassword(WebTarget target, MethodInfo method) {
        testFailedMethod(
            target,
            method,
            "wrong-user",
            "wrong-password",
            null,
            Response.Status.UNAUTHORIZED
        );
    }

    protected void testWrongRole(WebTarget target, MethodInfo method, String login, String password) {
        testFailedMethod(
            target,
            method,
            login,
            password,
            null,
            Response.Status.FORBIDDEN
        );
    }

    @Test
    public void testUnprotectedMethods() {
        WebTarget target = baseClient.target(getBaseUri());
        for (MethodInfo method : getAllMethods())
            testUnprotected(target, method);
    }

    @Test
    public void testWrongPasswordMethods() {
        WebTarget target = authClient.target(getBaseUri());
        for (MethodInfo method : getAllMethods())
            testWrongPassword(target, method);
    }

    protected void testWrongRoleMethods(String login, String password) {
        WebTarget target = authClient.target(getBaseUri());
        for (MethodInfo method : getAllMethods())
            testWrongRole(target, method, login, password);
    }

}