package com.monjam.core.command;

import com.monjam.core.api.Context;
import com.monjam.core.api.MigrationType;
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
import java.util.List;

public class DbRollback extends Command {
    private static final Logger LOG = LoggerFactory.getLogger(DbRollback.class);

    public DbRollback(Configuration configuration) {
        super(configuration);
    }

    @Override
    protected void doExecute(Context context, MigrationResolver migrationResolver, MigrationHistory migrationHistory) {
        List<ResolvedMigration> resolvedMigrations = migrationResolver.resolveMigrations(MigrationType.ROLLBACK);
        List<AppliedMigration> appliedMigrations = migrationHistory.getAppliedMigrations();

        final AppliedMigration lastAppliedMigration = appliedMigrations.size() > 0
                ? appliedMigrations.get(appliedMigrations.size() - 1)
                : null;
        if (lastAppliedMigration == null) {
            LOG.info("No applied migrations to rollback");
            return;
        }

        context.setMigrationType(MigrationType.ROLLBACK);
        LOG.info("Last applied migration version {}", lastAppliedMigration.getVersion());
        ResolvedMigration resolvedMigration = resolvedMigrations.stream()
                .filter(m -> lastAppliedMigration.getVersion().equals(m.getVersion()))
                .findFirst()
                .orElseGet(() -> {
                    LOG.warn("No rollback migration version {} was found. Executing noop rollback", lastAppliedMigration.getVersion());
                    return new NoopResolvedMigration(lastAppliedMigration.getVersion(), lastAppliedMigration.getDescription());
                });
        LOG.info("Execute rollback schema migration version {}", resolvedMigration.getVersion());
        if (context.isSupportTransaction()) {
            new TransactionTemplate().executeInTransaction(context, ctx ->
                    revertMigration(context, resolvedMigration, migrationHistory)
            );
        } else {
            revertMigration(context, resolvedMigration, migrationHistory);
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
