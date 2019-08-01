package com.monjam.core.command;

import com.monjam.core.api.Context;
import com.monjam.core.api.MigrationType;
import com.monjam.core.api.MigrationVersion;
import com.monjam.core.api.MonJamException;
import com.monjam.core.configuration.Configuration;
import com.monjam.core.database.TransactionTemplate;
import com.monjam.core.history.AppliedMigration;
import com.monjam.core.history.MigrationHistory;
import com.monjam.core.resolve.MigrationResolver;
import com.monjam.core.resolve.NoopResolvedMigration;
import com.monjam.core.resolve.ResolvedMigration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DbRollback extends Command {
    private static final Logger LOG = LoggerFactory.getLogger(DbRollback.class);

    public DbRollback(Configuration configuration) {
        super(configuration);
    }

    @Override
    protected void doExecute(Context context, MigrationResolver migrationResolver, MigrationHistory migrationHistory) {
        Map<MigrationVersion, ResolvedMigration> resolvedMigrations = migrationResolver.resolveMigrations(MigrationType.ROLLBACK).stream()
                .collect(Collectors.toMap(ResolvedMigration::getVersion, Function.identity()));
        List<AppliedMigration> appliedMigrations = migrationHistory.getAppliedMigrations();

        if (appliedMigrations.isEmpty()) {
            LOG.info("No applied migrations to rollback");
            return;
        }
        appliedMigrations.sort(Comparator.comparing(AppliedMigration::getVersion).reversed());
        MigrationVersion targetVersion = null;
        if (configuration.getTarget() == null) {
            // Default to previous version
            targetVersion = appliedMigrations.size() > 1 ? appliedMigrations.get(1).getVersion() : MigrationVersion.EMPTY;
        } else {
            targetVersion = new MigrationVersion(configuration.getTarget());
        }
        AppliedMigration lastAppliedMigration = appliedMigrations.get(0);
        if (targetVersion.compareTo(lastAppliedMigration.getVersion()) > 0) {
            throw new MonJamException("Target version is greater than current applied migration version");
        }
        if (!targetVersion.equals(MigrationVersion.EMPTY) && !resolvedMigrations.containsKey(targetVersion)) {
            throw new MonJamException("Target version " + targetVersion + " not found");
        }
        LOG.info("Execute rollback to version {}", targetVersion);
        context.setMigrationType(MigrationType.ROLLBACK);
        for (AppliedMigration appliedMigration : appliedMigrations) {
            if (appliedMigration.getVersion().compareTo(targetVersion) <= 0) {
                break;
            }
            if (!resolvedMigrations.containsKey(appliedMigration.getVersion())) {
                LOG.warn("No rollback migration version {} was found. Executing noop rollback", appliedMigration.getVersion());
            }
            ResolvedMigration resolvedMigration = resolvedMigrations.getOrDefault(appliedMigration.getVersion(), new NoopResolvedMigration(appliedMigration.getVersion(), appliedMigration.getDescription()));
            LOG.info("Execute rollback migration version {}", resolvedMigration.getVersion());
            if (context.isSupportTransaction()) {
                new TransactionTemplate().executeInTransaction(context, ctx ->
                        revertMigration(context, resolvedMigration, migrationHistory)
                );
            } else {
                revertMigration(context, resolvedMigration, migrationHistory);
            }
        }
    }

    public void revertMigration(Context context, ResolvedMigration resolvedMigration, MigrationHistory migrationHistory) {
        resolvedMigration.getExecutor().execute(context);
        migrationHistory.removeAppliedMigration(new AppliedMigration(
                resolvedMigration.getVersion(),
                resolvedMigration.getDescription(),
                ZonedDateTime.now()
        ));
    }
}
