package com.monjam.core.resolve;

import com.monjam.core.api.MonJamException;

public class MigrationInfoHelper {
    public static MigrationInfo extract(String migrationName) {
        if (migrationName == null) {
            throw new IllegalArgumentException("Migration name must not be null");
        }
        String[] parts = migrationName.split("__");
        if (parts.length < 2) {
            throw new MonJamException("Migration name should have version and description");
        }
        if (!parts[0].startsWith("V")) {
            throw new MonJamException("Migration version should have prefix 'V'");
        }
        String version = parts[0].substring(1).replace("_", ".");
        String description = parts.length > 1 ? parts[1].replace("_", " ") : "";
        return new MigrationInfo(version, description);
    }
}
