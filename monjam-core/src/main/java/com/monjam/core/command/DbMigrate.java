package com.monjam.core.command;

import com.mongodb.client.MongoDatabase;
import com.monjam.core.api.Configuration;
import com.monjam.core.api.Context;
import com.monjam.core.api.MigrationVersion;
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
    protected void doExecute(MongoDatabase database, MigrationResolver migrationResolver, MigrationHistory migrationHistory) {
        List<ResolvedMigration> resolvedMigrations = migrationResolver.resolveMigrations();
        List<AppliedMigration> appliedMigrations = migrationHistory.getAppliedMigrations();

        MigrationVersion currentVersion = null;
        if (appliedMigrations.size() > 0) {
            AppliedMigration lastAppliedMigration = appliedMigrations.get(appliedMigrations.size() - 1);
            currentVersion = lastAppliedMigration.getVersion();
            LOG.info("Last applied migration version {}", currentVersion);
        } else {
            LOG.info("No applied migrations found");
        }

        Context context = new Context(database);

        for (ResolvedMigration resolvedMigration : resolvedMigrations) {
            if (currentVersion != null && currentVersion.compareTo(resolvedMigration.getVersion()) >= 0) {
                continue;
            }
            LOG.info("Execute schema migration version {}", resolvedMigration.getVersion());
            resolvedMigration.getExecutor().executeUp(context);

            migrationHistory.addAppliedMigration(new AppliedMigration(
                    resolvedMigration.getVersion(),
                    resolvedMigration.getDescription(),
                    ZonedDateTime.now()
            ));
        }
    }
}
