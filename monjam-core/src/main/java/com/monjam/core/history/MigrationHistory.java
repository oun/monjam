package com.monjam.core.history;

import java.util.List;

public interface MigrationHistory {
    List<AppliedMigration> getAppliedMigrations();

    void addAppliedMigration(AppliedMigration appliedMigration);

    void removeAppliedMigration(AppliedMigration appliedMigration);
}
