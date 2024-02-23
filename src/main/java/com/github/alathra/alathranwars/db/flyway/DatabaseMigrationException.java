package com.github.alathra.alathranwars.db.flyway;

public class DatabaseMigrationException extends Exception {
    public DatabaseMigrationException(Throwable t) {
        super(t);
    }
}
