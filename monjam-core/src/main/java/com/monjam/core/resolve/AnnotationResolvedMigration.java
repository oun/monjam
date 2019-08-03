package com.monjam.core.resolve;

import com.monjam.core.api.MigrationVersion;
import com.monjam.core.executor.AnnotationMigrationExecutor;
import com.monjam.core.executor.MigrationExecutor;

public class AnnotationResolvedMigration implements ResolvedMigration {
    private AnnotationMigrationExecutor executor;
    private MigrationVersion version;
    private String description;

    public AnnotationResolvedMigration(MigrationVersion version, String description, AnnotationMigrationExecutor executor) {
        this.version = version;
        this.description = description;
        this.executor = executor;
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
