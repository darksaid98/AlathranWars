package com.github.alathra.AlathranWars.db.flyway;

public class DatabaseMigrationException extends Exception {
    public DatabaseMigrationException(Throwable t) {
        super(t);
    }
}
