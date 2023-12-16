package com.github.alathra.AlathranWars.db.flyway.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V3__Example extends BaseJavaMigration {
    @Override
    public void migrate(Context flywayContext) throws Exception {
        /*try {
            final DSLContext context = DB.getContext(flywayContext.getConnection());

            context.alterTable(TEST).addColumn(field(name("meme"), VARCHAR)).execute();

            context
                .alterTableIfExists(TEST)
                .addColumnIfNotExists(field(name("column_name"), VARCHAR(32).notNull()*//*.defaultValue("default")*//*))
                .execute();
        } catch (Exception e) {
            Logger.get().error(e.getMessage());
        }*/
    }
}
