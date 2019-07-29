package com.monjam.core.executor;

import com.monjam.core.api.Context;
import com.monjam.core.api.Migration;
import com.monjam.core.api.MigrationType;

public class JavaMigrationExecutor implements MigrationExecutor {
    private Migration migration;

    public JavaMigrationExecutor(Migration migration) {
        this.migration = migration;
    }

    @Override
    public void execute(Context context) {
        if (context.getMigrationType() == MigrationType.MIGRATE) {
            migration.up(context);
        } else if (context.getMigrationType() == MigrationType.ROLLBACK) {
            migration.down(context);
        }
    }
}
