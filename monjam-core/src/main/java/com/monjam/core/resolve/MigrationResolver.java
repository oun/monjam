package com.monjam.core.resolve;

import java.util.List;

public interface MigrationResolver {
    List<ResolvedMigration> resolveMigrations();
}
