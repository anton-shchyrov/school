package org.example.school.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.helidon.microprofile.tests.junit5.HelidonTest;
import org.example.school.protocol.Config;
import org.example.school.protocol.ServiceConfig;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.io.InputStreamReader;

@HelidonTest
class ConfigServiceTest {
    private static final Gson gson = new GsonBuilder().create();

    @Test
    public void testConfig(WebTarget target) {
        try (Response response = target
            .path("/config")
            .request(MediaType.APPLICATION_JSON)
            .get())
        {
            MatcherAssert.assertThat(response.getStatus(), Matchers.is(200));
            InputStream inputStream = response.readEntity(InputStream.class);
            Config config = gson.fromJson(new InputStreamReader(inputStream), Config.class);
            MatcherAssert.assertThat(config.services, Matchers.notNullValue());
            MatcherAssert.assertThat(config.services, Matchers.hasKey("users"));
        }
    }

    @Test
    public void testServiceUsers(WebTarget target) {
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
    public void testServiceUnknown(WebTarget target) {
        try (Response response = target
            .path("/config/services/missed-service")
            .request(MediaType.APPLICATION_JSON)
            .get())
        {
            MatcherAssert.assertThat(response.getStatus(), Matchers.is(404));
        }
    }
}