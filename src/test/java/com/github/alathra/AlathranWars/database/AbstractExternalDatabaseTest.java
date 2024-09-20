package com.github.alathra.AlathranWars.database;

import com.github.alathra.alathranwars.database.DatabaseType;
import com.github.alathra.alathranwars.database.handler.DatabaseHandler;
import com.github.alathra.alathranwars.database.config.DatabaseConfigBuilder;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Tag("externaldatabase")
@Testcontainers(disabledWithoutDocker = true)
public abstract class AbstractExternalDatabaseTest extends AbstractDatabaseTest {
    @Container
    public static GenericContainer<?> container;

    public AbstractExternalDatabaseTest(String jdbcPrefix, DatabaseType requiredDatabaseType, GenericContainer<?> container) {
        super(jdbcPrefix, requiredDatabaseType);
        AbstractExternalDatabaseTest.container = container;
        container.start();
    }

    @BeforeAll
    @DisplayName("Initialize connection pool")
    void beforeAllTests() {
        Assertions.assertTrue(container.isRunning());

        databaseConfig = new DatabaseConfigBuilder()
            .withDatabaseType(jdbcPrefix)
            .withDatabase("testing")
            .withHost(container.getHost())
            .withPort(container.getFirstMappedPort())
            .withUsername("root")
            .withPassword("")
            .build();
        Assertions.assertEquals(requiredDatabaseType, databaseConfig.getDatabaseType());

        databaseHandler = new DatabaseHandler(databaseConfig, logger);
        databaseHandler.startup();
    }

    @AfterAll
    void afterAllTests() {
        container.stop();
    }
}
