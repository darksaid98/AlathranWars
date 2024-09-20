package com.github.alathra.AlathranWars.database;

import com.github.alathra.alathranwars.database.DatabaseType;
import com.github.alathra.alathranwars.database.handler.DatabaseHandler;
import com.github.alathra.alathranwars.database.config.DatabaseConfigBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

@Tag("embeddeddatabase")
public abstract class AbstractEmbeddedDatabaseTest extends AbstractDatabaseTest {
    static @TempDir Path TEMP_DIR;

    public AbstractEmbeddedDatabaseTest(String jdbcPrefix, DatabaseType requiredDatabaseType) {
        super(jdbcPrefix, requiredDatabaseType);
    }

    @BeforeAll
    @DisplayName("Initialize connection pool")
    void beforeAllTests() {
        databaseConfig = new DatabaseConfigBuilder()
            .withDatabaseType(jdbcPrefix)
            .withPath(TEMP_DIR)
            .build();
        Assertions.assertEquals(requiredDatabaseType, databaseConfig.getDatabaseType());

        databaseHandler = new DatabaseHandler(databaseConfig, logger);
        databaseHandler.startup();
    }
}
