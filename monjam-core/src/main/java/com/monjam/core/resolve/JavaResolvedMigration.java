package com.monjam.core.resolve;

import com.monjam.core.api.Migration;
import com.monjam.core.api.MigrationVersion;
import com.monjam.core.executor.JavaMigrationExecutor;
import com.monjam.core.executor.MigrationExecutor;

public class JavaResolvedMigration implements ResolvedMigration {
    private JavaMigrationExecutor executor;
    private MigrationVersion version;
    private String description;

    public JavaResolvedMigration(MigrationVersion version, String description, Migration migration) {
        this(version, description, new JavaMigrationExecutor(migration));
    }

    public JavaResolvedMigration(MigrationVersion version, String description, JavaMigrationExecutor executor) {
        this.executor = executor;
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
        return executor;
    }
}
