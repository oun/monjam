package com.monjam.core.executor;

import com.monjam.core.api.Context;
import com.monjam.core.api.Migration;

public class JavaMigrationExecutor implements MigrationExecutor {
    private Migration migration;

    public JavaMigrationExecutor(Migration migration) {
        this.migration = migration;
    }

    @Override
    public void executeUp(Context context) {
        migration.up(context);
    }

    @Override
    public void executeDown(Context context) {
        migration.down(context);
    }
}
