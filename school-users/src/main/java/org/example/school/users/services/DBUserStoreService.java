package org.example.school.users.services;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.jdbc.JdbcDbClientProvider;
import io.helidon.security.providers.httpauth.SecureUserStore;
import io.helidon.security.providers.httpauth.spi.UserStoreService;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

public class DBUserStoreService implements UserStoreService {
    private static final String PROPERTIES_RESOURCE = "META-INF/microprofile-config.properties";
    private static final String PROPERTY_PREFIX = "javax.sql.DataSource.school.";
    private static final int PROPERTY_PREFIX_LEN = PROPERTY_PREFIX.length();

    private final DbClient dbClient;

    public DBUserStoreService() {
        Properties props = getDataSourceProperties();
        DataSource dataSource = new HikariDataSource(new HikariConfig(props));
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

    private Properties getDataSourceProperties() {
        Properties props = new Properties();
        try (
            InputStream stream = getClass().getClassLoader().getResourceAsStream(PROPERTIES_RESOURCE)
        ) {
            props.load(stream);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + PROPERTIES_RESOURCE + " resource", e);
        }
        return filterDataSourceProperties(props);
    }

    private static Properties filterDataSourceProperties(Properties input) {
        Properties res = new Properties();
        input.forEach((key, value) -> {
            if (key != null) {
                String keyStr = key.toString();
                if (keyStr.startsWith(PROPERTY_PREFIX)) {
                    res.put(keyStr.substring(PROPERTY_PREFIX_LEN), value);
                }
            }
        });
        return res;
    }

    @Override
    public String configKey() {
        return "school-auth";
    }

    @Override
    public SecureUserStore create(Config config) {
        return new UserStore(dbClient);
    }
}