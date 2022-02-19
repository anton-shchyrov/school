package org.example.school.users.resources;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.jdbc.JdbcDbClientProvider;
import org.example.school.users.adapters.LocalDateAdapter;
import org.example.school.users.adapters.StudentTrackItemFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.LocalDate;

public class CustomResource {
    public static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
        .registerTypeAdapterFactory(new StudentTrackItemFactory())
        .setPrettyPrinting()
        .create();
    private static final Config STATEMENTS = Config.create().get("db.statements");

    private final DbClient dbClient;

    public CustomResource(DataSource dataSource) {
        dbClient = new JdbcDbClientProvider().builder()
            .connectionPool(() -> {
                try {
                    return dataSource.getConnection();
                } catch (SQLException e) {
                    throw new IllegalStateException("Error while setting up new connection", e);
                }
            }).config(Config.create().get("db"))
            .build();
    }

    protected DbClient getDbClient() {
        return dbClient;
    }

    protected static String getStatement(String name) {
        return STATEMENTS.get(name).asString().orElseThrow(
            () -> new RuntimeException(String.format("Statement \"%s\" not found", name))
        );
    }
}
