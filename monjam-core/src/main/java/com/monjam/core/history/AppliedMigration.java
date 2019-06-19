package com.monjam.core.history;

import com.monjam.core.api.MigrationVersion;

import java.time.LocalDateTime;

public class AppliedMigration {
    private MigrationVersion version;
    private String description;
    private LocalDateTime executedAt;

    public AppliedMigration(MigrationVersion version, String description, LocalDateTime executedAt) {
        this.version = version;
        this.description = description;
        this.executedAt = executedAt;
    }

    public MigrationVersion getVersion() {
        return version;
    }

    public void setVersion(MigrationVersion version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }
}
