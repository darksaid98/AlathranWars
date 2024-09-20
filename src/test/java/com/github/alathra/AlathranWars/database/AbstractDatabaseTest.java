package com.github.alathra.AlathranWars.database;

import com.github.alathra.alathranwars.database.DatabaseType;
import com.github.alathra.alathranwars.database.handler.DatabaseHandler;
import com.github.alathra.alathranwars.database.config.DatabaseConfig;
import com.github.alathra.alathranwars.database.exception.DatabaseMigrationException;
import com.github.alathra.alathranwars.database.migration.MigrationHandler;
import com.github.alathra.alathranwars.database.jooq.JooqContext;
import org.jooq.DSLContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.github.alathra.alathranwars.database.QueryUtil.fromUUIDToBytes;
import static com.github.alathra.alathranwars.database.schema.Tables.*;
import static com.github.alathra.alathranwars.database.schema.Tables.LIST;

@Tag("database")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
abstract class AbstractDatabaseTest {
    public String jdbcPrefix;
    public DatabaseType requiredDatabaseType;
    public DatabaseConfig databaseConfig;
    public DatabaseHandler databaseHandler;
    public Logger logger = LoggerFactory.getLogger("Database Test Logger");
    @SuppressWarnings("unused")
    static List<String> tablePrefixes = Arrays.asList("", "test_", "somelongprefix_");

    public AbstractDatabaseTest(String jdbcPrefix, DatabaseType requiredDatabaseType) {
        this.jdbcPrefix = jdbcPrefix;
        this.requiredDatabaseType = requiredDatabaseType;
    }

    @BeforeEach
    void beforeEachTest() {
    }

    @AfterEach
    void afterEachTest() {
    }

    @AfterAll
    void afterAllTests() {
        databaseHandler.shutdown();
    }

    // Shared tests

    @ParameterizedTest
    @FieldSource("tablePrefixes")
    @Order(1)
    @DisplayName("Flyway migrations")
    void testMigrations(String prefix) throws DatabaseMigrationException {
        databaseHandler.getDatabaseConfig().setTablePrefix(prefix);
        new MigrationHandler(
            databaseHandler.getConnectionPool(),
            databaseHandler.getDatabaseConfig()
        )
            .migrate();
    }

    @ParameterizedTest
    @FieldSource("tablePrefixes")
    @DisplayName("Select query")
    void testQuerySelect(String prefix) throws SQLException {
        databaseHandler.getDatabaseConfig().setTablePrefix(prefix);
        JooqContext jooqContext = new JooqContext(databaseHandler.getDatabaseConfig());

        Connection con = databaseHandler.getConnection();
        DSLContext context = jooqContext.createContext(con);
        context
            .select(SIDES_PLAYERS.SIDE, SIDES_PLAYERS.PLAYER, SIDES_PLAYERS.SURRENDERED)
            .from(SIDES_PLAYERS)
            .fetch();
        con.close();
    }

    @ParameterizedTest
    @FieldSource("tablePrefixes")
    @DisplayName("Insert query")
    void testQueryInsert(String prefix) throws SQLException {
        databaseHandler.getDatabaseConfig().setTablePrefix(prefix);
        JooqContext jooqContext = new JooqContext(databaseHandler.getDatabaseConfig());

        Connection con = databaseHandler.getConnection();
        DSLContext context = jooqContext.createContext(con);

        final UUID warUUID = UUID.randomUUID();
        final UUID side1UUID = UUID.randomUUID();
        final UUID side2UUID = UUID.randomUUID();

        // Insert war
        context
            .insertInto(LIST, LIST.UUID, LIST.NAME, LIST.LABEL, LIST.SIDE1, LIST.SIDE2, LIST.EVENT)
            .values(
                fromUUIDToBytes(warUUID),
                "test",
                "",
                fromUUIDToBytes(side1UUID),
                fromUUIDToBytes(side2UUID),
                (byte) 1
            )
            .onDuplicateKeyUpdate()
            .set(LIST.NAME, "")
            .set(LIST.LABEL, "")
            .set(LIST.SIDE1, fromUUIDToBytes(side1UUID))
            .set(LIST.SIDE2, fromUUIDToBytes(side2UUID))
            .execute();

        // Insert sides
        context
            .insertInto(SIDES, SIDES.WAR, SIDES.UUID, SIDES.SIDE, SIDES.TEAM, SIDES.NAME, SIDES.TOWN, SIDES.SIEGE_GRACE, SIDES.RAID_GRACE)
            .values(
                fromUUIDToBytes(warUUID),
                fromUUIDToBytes(side1UUID),
                "",
                "",
                "",
                fromUUIDToBytes(UUID.randomUUID()),
                LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC),
                LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC)
            )
            .onDuplicateKeyUpdate()
            .set(SIDES.SIDE, "")
            .set(SIDES.TEAM, "")
            .set(SIDES.NAME, "")
            .set(SIDES.TOWN, fromUUIDToBytes(UUID.randomUUID()))
            .set(SIDES.SIEGE_GRACE, LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC))
            .set(SIDES.RAID_GRACE, LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC))
            .execute();

        context
            .insertInto(SIDES, SIDES.WAR, SIDES.UUID, SIDES.SIDE, SIDES.TEAM, SIDES.NAME, SIDES.TOWN, SIDES.SIEGE_GRACE, SIDES.RAID_GRACE)
            .values(
                fromUUIDToBytes(warUUID),
                fromUUIDToBytes(side2UUID),
                "",
                "",
                "",
                fromUUIDToBytes(UUID.randomUUID()),
                LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC),
                LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC)
            )
            .onDuplicateKeyUpdate()
            .set(SIDES.SIDE, "")
            .set(SIDES.TEAM, "")
            .set(SIDES.NAME, "")
            .set(SIDES.TOWN, fromUUIDToBytes(UUID.randomUUID()))
            .set(SIDES.SIEGE_GRACE, LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC))
            .set(SIDES.RAID_GRACE, LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC))
            .execute();

        con.close();
    }

    @ParameterizedTest
    @FieldSource("tablePrefixes")
    @DisplayName("Delete query")
    void testQuerySet(String prefix) throws SQLException {
        databaseHandler.getDatabaseConfig().setTablePrefix(prefix);
        JooqContext jooqContext = new JooqContext(databaseHandler.getDatabaseConfig());

        Connection con = databaseHandler.getConnection();
        DSLContext context = jooqContext.createContext(con);
        context
            .deleteFrom(LIST)
            .where(LIST.NAME.equal("test"))
            .execute();
        con.close();
    }
}
