package com.monjam.core.resolve;

import com.monjam.core.api.JavaMigration;
import com.monjam.core.api.MigrationVersion;
import com.monjam.core.executor.JavaMigrationExecutor;
import com.monjam.core.executor.MigrationExecutor;

public class JavaResolvedMigration implements ResolvedMigration {
    private JavaMigration migration;
    private MigrationVersion version;
    private String description;

    public JavaResolvedMigration(MigrationVersion version, String description, JavaMigration migration) {
        this.migration = migration;
        this.version = version;
        this.description = description;
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
        return new JavaMigrationExecutor(migration);
    }
}
