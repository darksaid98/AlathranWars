package com.github.alathra.AlathranWars.utility;

import com.github.alathra.AlathranWars.Main;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class DB {
    public static Connection get() throws SQLException {
        return Main.getInstance().getDataManager().getConnection();
    }

    public static HikariDataSource getDataSource() {
        return Main.getInstance().getDataManager().getDataSource();
    }
}
