package com.monjam.core.resolve;

import com.monjam.core.api.MigrationType;

import java.util.List;

public interface MigrationResolver {
    List<ResolvedMigration> resolveMigrations(MigrationType type);
}
