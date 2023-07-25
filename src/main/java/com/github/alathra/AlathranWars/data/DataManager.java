package com.github.alathra.AlathranWars.data;

import com.github.alathra.AlathranWars.Main;
import com.github.alathra.AlathranWars.utility.Config;
import com.github.alathra.AlathranWars.utility.SQLQueries;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class DataManager {
    private final Main instance;
    private HikariConfig hikariConfig;
    private HikariDataSource hikariDataSource;

    public DataManager(Main instance) {
        this.instance = instance;
    }

    public void onLoad() {
        openConnection();
        SQLQueries.initDB();
    }

    public void onEnable() {
    }

    public void onDisable() {
        closeDatabaseConnection();
    }

    @NotNull
    public Connection getConnection() throws SQLException {
        if (hikariDataSource == null)
            throw new SQLException("Unable to get a connection from the pool. (hikariDataSource is null)");

        Connection connection = hikariDataSource.getConnection();
        if (connection == null)
            throw new SQLException("Unable to get a connection from the pool. (HikariDataSource#getConnection returned null)");

        return connection;
    }

    @NotNull
    public HikariDataSource getDataSource() {
        return hikariDataSource;
    }

    public void openConnection() {
        if (hikariDataSource != null)
            return;

        hikariConfig = new HikariConfig();

        final boolean mysqlEnabled = Config.get().getBoolean("mysql.enabled");

        if (mysqlEnabled) {
            hikariConfig.setDataSourceClassName("org.mariadb.jdbc.MariaDbDataSource");
            hikariConfig.addDataSourceProperty("url", "jdbc:mariadb://%s/%s".formatted(
                Config.get().get("mysql.host", "127.0.0.1:3306"),
                Config.get().get("mysql.database", "database")
            ));
            hikariConfig.setUsername(Config.get().get("mysql.user", "username"));
            hikariConfig.setPassword(Config.get().get("mysql.pass", "123"));
            hikariConfig.setConnectionTimeout(5000);
            hikariConfig.setKeepaliveTime(0);
        } else {
            @NotNull File dataFolder = new File(instance.getDataFolder(), "database.db");

            if (!dataFolder.exists()) {
                try {
                    if (!dataFolder.createNewFile())
                        instance.getLogger().severe("File write error: database.db (1)");
                } catch (IOException e) {
                    instance.getLogger().severe("File write error: database.db (2) - %s".formatted(e.getMessage()));
                }
            }

            hikariConfig.setJdbcUrl("jdbc:sqlite:" + dataFolder);
            hikariConfig.setConnectionInitSql("PRAGMA journal_mode=WAL; PRAGMA busy_timeout=30000");
            hikariConfig.setConnectionTimeout(30000);
        }

        hikariConfig.setPoolName("althranwars-hikari");
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(10);
        hikariConfig.setInitializationFailTimeout(-1); // We try to create tables after this anyways which will error if no connection
//        hikariConfig.addDataSourceProperty("characterEncoding", "UTF-8");

        // General optimizations (https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration)
//        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
//        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
//        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
//        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
//        hikariConfig.addDataSourceProperty("useLocalSessionState", "true");
//        hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
//        hikariConfig.addDataSourceProperty("cacheServerConfiguration", "true");
//        hikariConfig.addDataSourceProperty("maintainTimeStats", "false");
//        hikariConfig.addDataSourceProperty("allowPublicKeyRetrieval", "true");
//        hikariConfig.addDataSourceProperty("useSSL", "false");

        hikariDataSource = new HikariDataSource(hikariConfig);
    }

    public void closeDatabaseConnection() {
        instance.getLogger().info("Closing database connection...");

        if (hikariDataSource != null) {
            if (!hikariDataSource.isClosed()) {
                try {
                    hikariDataSource.close();
                } catch (Exception e) {
                    instance.getLogger().severe("Error closing database connections:");
                    e.printStackTrace();
                }
            } else {
                instance.getLogger().info("Skipped closing database connection: connection is already closed.");
            }
        } else {
            instance.getLogger().severe("Skipped closing database connection because the data source is null. Was there a previous error which needs to be fixed? Check your console logs!");
        }
    }
}
