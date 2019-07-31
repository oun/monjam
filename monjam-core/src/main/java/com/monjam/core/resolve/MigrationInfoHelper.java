package com.monjam.core.resolve;

import com.monjam.core.api.MigrationType;
import com.monjam.core.api.MonJamException;

public class MigrationInfoHelper {
    private MigrationInfoHelper() {
        throw new IllegalStateException("This class should not instantiate");
    }

    public static MigrationInfo extract(String migrationName) {
        if (migrationName == null) {
            throw new IllegalArgumentException("Migration name must not be null");
        }
        String[] parts = migrationName.split("__");
        if (parts.length < 2) {
            throw new MonJamException("Migration name should have version and description");
        }
        if (!parts[0].startsWith("V") && !parts[0].startsWith("U")) {
            throw new MonJamException("Migration version should have prefix 'V' or 'U'");
        }
        MigrationType type = MigrationType.fromString(parts[0].substring(0, 1));
        String version = parts[0].substring(1).replace("_", ".");
        String description = parts[1].replace("_", " ");
        return new MigrationInfo(version, type, description);
    }
}
