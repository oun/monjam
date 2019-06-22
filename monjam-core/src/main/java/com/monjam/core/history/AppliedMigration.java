package com.monjam.core.history;

import com.monjam.core.api.MigrationVersion;

import java.time.ZonedDateTime;
import java.util.Objects;

public class AppliedMigration {
    private MigrationVersion version;
    private String description;
    private ZonedDateTime executedAt;

    public AppliedMigration(MigrationVersion version, String description, ZonedDateTime executedAt) {
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

    public ZonedDateTime getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(ZonedDateTime executedAt) {
        this.executedAt = executedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppliedMigration that = (AppliedMigration) o;
        return version.equals(that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version);
    }
}
