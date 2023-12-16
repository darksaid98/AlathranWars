package com.github.alathra.AlathranWars.utility;

import com.github.alathra.AlathranWars.AlathranWars;
import com.github.alathra.AlathranWars.db.DatabaseHandler;
import com.github.alathra.AlathranWars.db.DatabaseType;
import com.github.alathra.AlathranWars.db.jooq.JooqContext;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Convenience class for accessing methods in {@link DatabaseHandler#getConnection}
 */
public abstract class DB {
    /**
     * Convenience method for {@link DatabaseHandler#getConnection} to getConnection {@link Connection}
     */
    @NotNull
    public static Connection getConnection() throws SQLException {
        return AlathranWars.getInstance().getDataHandler().getConnection();
    }

    /**
     * Convenience method for {@link JooqContext#createContext(Connection)} to getConnection {@link DSLContext}
     */
    @NotNull
    public static DSLContext getContext(Connection con) {
        return AlathranWars.getInstance().getDataHandler().getJooqContext().createContext(con);
    }

    /**
     * Convenience method for {@link DatabaseHandler#getDB()} to getConnection {@link DatabaseType}
     */
    public static DatabaseType getDB() {
        return AlathranWars.getInstance().getDataHandler().getDB();
    }
}
