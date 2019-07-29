package com.monjam.core.resolve;

import com.monjam.core.api.MigrationVersion;
import com.monjam.core.executor.MigrationExecutor;
import com.monjam.core.executor.ScriptMigrationExecutor;

public class ScriptResolvedMigration implements ResolvedMigration {
    private ScriptMigrationExecutor executor;
    private MigrationVersion version;
    private String description;

    public ScriptResolvedMigration(MigrationVersion version, String description, String script) {
        this(version, description, new ScriptMigrationExecutor(script));
    }

    public ScriptResolvedMigration(MigrationVersion version, String description, ScriptMigrationExecutor executor) {
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
