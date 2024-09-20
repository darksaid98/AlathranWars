package com.github.alathra.alathranwars.database.migration.migrations;

import com.github.alathra.alathranwars.utility.DB;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.jooq.DSLContext;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;
import static org.jooq.impl.SQLDataType.VARCHAR;

/**
 * Example Java Flyway migration using jOOQ.
 */
public class V3__Example extends BaseJavaMigration {
    @Override
    public void migrate(final Context flywayContext) throws Exception {
        try {
            final DSLContext context = DB.getContext(flywayContext.getConnection());

            context
                .alterTable(table("test"))
                .addColumn(field("memes", VARCHAR((32))))
                .execute();

            context
                .alterTableIfExists(table("test"))
                .addColumnIfNotExists(field("column_name", VARCHAR(32).notNull()/*.defaultValue("default")*/))
                .execute();
        } catch (Exception e) {
//            Logger.pool().error(e.getMessage());
        }
    }
}
