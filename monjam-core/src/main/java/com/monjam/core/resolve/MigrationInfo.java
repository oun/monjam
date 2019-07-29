package com.monjam.core.resolve;

import com.monjam.core.api.MigrationType;
import com.monjam.core.api.MigrationVersion;

public class MigrationInfo {
    private MigrationVersion version;
    private MigrationType type;
    private String description;

    public MigrationInfo(String version, MigrationType type, String description) {
        this.version = new MigrationVersion(version);
        this.type = type;
        this.description = description;
    }

    public MigrationVersion getVersion() {
        return version;
    }

    public MigrationType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
}
