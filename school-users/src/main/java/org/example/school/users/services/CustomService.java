package org.example.school.users.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.helidon.common.context.Contexts;
import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;
import org.example.school.users.adapters.LocalDateAdapter;
import org.example.school.users.adapters.StudentTrackItemFactory;

import java.time.LocalDate;

public class CustomService {
    public static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
        .registerTypeAdapterFactory(new StudentTrackItemFactory())
        .setPrettyPrinting()
        .create();
    private static final Config STATEMENTS = Config.create().get("db.statements");

    protected static DbClient getDbClient() {
        return Contexts.globalContext().get(DbClient.class).orElseThrow();
    }

    protected static String getStatement(String name) {
        return STATEMENTS.get(name).asString().orElseThrow(
            () -> new RuntimeException(String.format("Statement \"%s\" not found", name))
        );
    }
}
