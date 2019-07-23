package com.monjam.core.command;

import com.monjam.core.api.Configuration;
import com.monjam.core.api.Context;
import com.monjam.core.api.MonJamException;
import com.monjam.core.database.TransactionTemplate;
import com.monjam.core.history.AppliedMigration;
import com.monjam.core.history.MigrationHistory;
import com.monjam.core.resolve.MigrationResolver;
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
        List<ResolvedMigration> resolvedMigrations = migrationResolver.resolveMigrations();
        List<AppliedMigration> appliedMigrations = migrationHistory.getAppliedMigrations();

        final AppliedMigration lastAppliedMigration = appliedMigrations.size() > 0
                ? appliedMigrations.get(appliedMigrations.size() - 1)
                : null;
        if (lastAppliedMigration == null) {
            LOG.info("No applied migrations to rollback");
            return;
        }

        LOG.info("Last applied migration version {}", lastAppliedMigration.getVersion());
        ResolvedMigration resolvedMigration = resolvedMigrations.stream()
                .filter(m -> lastAppliedMigration.getVersion().equals(m.getVersion()))
                .findFirst()
                .orElseThrow(() -> new MonJamException("Could not execute rollback migration version " + lastAppliedMigration.getVersion() + ". Migration not found"));
        LOG.info("Execute down schema migration version {}", resolvedMigration.getVersion());
        if (context.isSupportTransaction()) {
            new TransactionTemplate().executeInTransaction(context, ctx ->
                    revertMigration(context, resolvedMigration, migrationHistory)
            );
        } else {
            revertMigration(context, resolvedMigration, migrationHistory);
        }
    }

    public void revertMigration(Context context, ResolvedMigration resolvedMigration, MigrationHistory migrationHistory) {
        resolvedMigration.getExecutor().executeDown(context);
        migrationHistory.removeAppliedMigration(new AppliedMigration(
                resolvedMigration.getVersion(),
                resolvedMigration.getDescription(),
                ZonedDateTime.now()
        ));
    }
}
