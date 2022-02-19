package org.example.school.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.school.protocol.Config;
import org.example.school.protocol.ServiceConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;

@ApplicationScoped
@Path("/config")
@Produces(MediaType.APPLICATION_JSON)
public class ConfigService {
    private static final String CONFIG_NAME = "config.json";
    private static final Gson GSON = new GsonBuilder().create();
    private static final Config CONFIG;

    static {
        try {
            String configFolder = new File(
                ConfigService.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI()
            ).getParent();
            java.nio.file.Path configFile = FileSystems.getDefault().getPath(configFolder, CONFIG_NAME);
            CONFIG = GSON.fromJson(new FileReader(configFile.toFile()), Config.class);
        } catch (URISyntaxException | FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @GET
    public String config() {
        return GSON.toJson(CONFIG);
    }

    @GET
    @Path("services/{name}")
    public Response service(@PathParam("name") String name) {
        ServiceConfig service = CONFIG.services.get(name);
        if (service != null)
        return Response.ok()
            .entity(GSON.toJson(service))
            .build();
        else
            return Response.status(Response.Status.NOT_FOUND).build();
    }
}
