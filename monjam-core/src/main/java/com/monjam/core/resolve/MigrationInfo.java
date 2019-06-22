package com.monjam.core.resolve;

import com.monjam.core.api.MigrationVersion;

public class MigrationInfo {
    private MigrationVersion version;
    private String description;

    public MigrationInfo(String version, String description) {
        this.version = new MigrationVersion(version);
        this.description = description;
    }

    public MigrationVersion getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }
}
