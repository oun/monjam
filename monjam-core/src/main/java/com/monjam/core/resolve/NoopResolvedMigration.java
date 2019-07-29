package com.monjam.core.resolve;

import com.monjam.core.api.MigrationVersion;
import com.monjam.core.executor.MigrationExecutor;
import com.monjam.core.executor.NoopMigrationExecutor;

public class NoopResolvedMigration implements ResolvedMigration {
    private MigrationVersion version;
    private String description;
    private MigrationExecutor executor;

    public NoopResolvedMigration(MigrationVersion version, String description) {
        this.version = version;
        this.description = description;
        this.executor = new NoopMigrationExecutor();
    }

    @Override
    public MigrationVersion getVersion() {
        return version;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public MigrationExecutor getExecutor() {
        return executor;
    }
}
