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
import com.monjam.core.resolve.ResolvedMigration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.List;

public class DbMigrate extends Command {
    private static final Logger LOG = LoggerFactory.getLogger(DbMigrate.class);

    public DbMigrate(Configuration configuration) {
        super(configuration);
    }

    @Override
    protected void doExecute(Context context, MigrationResolver migrationResolver, MigrationHistory migrationHistory) {
        List<ResolvedMigration> resolvedMigrations = migrationResolver.resolveMigrations(MigrationType.MIGRATE);
        List<AppliedMigration> appliedMigrations = migrationHistory.getAppliedMigrations();

        MigrationVersion currentVersion = null;
        if (appliedMigrations.size() > 0) {
            AppliedMigration lastAppliedMigration = appliedMigrations.get(appliedMigrations.size() - 1);
            currentVersion = lastAppliedMigration.getVersion();
            LOG.info("Last applied migration version {}", currentVersion);
        } else {
            currentVersion = MigrationVersion.EMPTY;
            LOG.info("No applied migrations found");
        }
        MigrationVersion targetVersion = configuration.getTarget() == null
                ? MigrationVersion.LATEST
                : new MigrationVersion(configuration.getTarget());
        LOG.info("Executing migration up to version {}", targetVersion);
        if (targetVersion.compareTo(currentVersion) < 0) {
            throw new MonJamException("Target version is less than current applied migration version");
        }
        if (!targetVersion.equals(MigrationVersion.LATEST)) {
            resolvedMigrations.stream().filter(resolvedMigration -> resolvedMigration.getVersion().equals(targetVersion)).findFirst()
                    .orElseThrow(() -> new MonJamException("Target version " + targetVersion + " not found"));
        }
        context.setMigrationType(MigrationType.MIGRATE);
        for (ResolvedMigration resolvedMigration : resolvedMigrations) {
            if (currentVersion.compareTo(resolvedMigration.getVersion()) >= 0) {
                continue;
            }
            if (resolvedMigration.getVersion().compareTo(targetVersion) > 0) {
                break;
            }
            LOG.info("Execute migration version {}", resolvedMigration.getVersion());
            if (context.isSupportTransaction()) {
                new TransactionTemplate().executeInTransaction(context, ctx ->
                        applyMigration(ctx, resolvedMigration, migrationHistory)
                );
            } else {
                applyMigration(context, resolvedMigration, migrationHistory);
            }
        }
    }

    public void applyMigration(Context context, ResolvedMigration resolvedMigration, MigrationHistory migrationHistory) {
        resolvedMigration.getExecutor().execute(context);
        migrationHistory.addAppliedMigration(new AppliedMigration(
                resolvedMigration.getVersion(),
                resolvedMigration.getDescription(),
                ZonedDateTime.now()
        ));
    }
}
