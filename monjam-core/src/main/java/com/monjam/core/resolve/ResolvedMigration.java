package com.monjam.core.resolve;

import com.monjam.core.api.MigrationVersion;
import com.monjam.core.executor.MigrationExecutor;

public interface ResolvedMigration {
    MigrationVersion getVersion();
    String getDescription();
    MigrationExecutor getExecutor();
}
