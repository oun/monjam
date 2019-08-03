package com.monjam.core.history;

import com.mongodb.client.model.Sorts;
import com.monjam.core.configuration.Configuration;
import com.monjam.core.api.MigrationVersion;
import com.monjam.core.database.DbTemplate;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class DbMigrationHistory implements MigrationHistory {
    private static final Logger LOG = LoggerFactory.getLogger(DbMigrationHistory.class);

    private static final String VERSION = "version";
    private static final String DESCRIPTION = "description";
    private static final String EXECUTED_AT = "executedAt";

    private final DbTemplate dbTemplate;
    private final Configuration configuration;

    public DbMigrationHistory(DbTemplate dbTemplate, Configuration configuration) {
        this.dbTemplate = dbTemplate;
        this.configuration = configuration;
        initialize();
    }

    protected void initialize() {
        LOG.debug("Create collection {} if it does not exists", configuration.getCollection());
        dbTemplate.createCollectionIfNotExists(configuration.getCollection());
    }

    @Override
    public List<AppliedMigration> getAppliedMigrations() {
        Collection<AppliedMigration> appliedMigrations = dbTemplate.find(Sorts.ascending(VERSION), configuration.getCollection(), document -> new AppliedMigration(
                new MigrationVersion(document.getString(VERSION)),
                document.getString(DESCRIPTION),
                ZonedDateTime.ofInstant(document.getDate(EXECUTED_AT).toInstant(), ZoneOffset.UTC.normalized())
        ));
        return new ArrayList<>(appliedMigrations);
    }

    @Override
    public void addAppliedMigration(AppliedMigration appliedMigration) {
        dbTemplate.insert(appliedMigration, configuration.getCollection(), migration -> new Document()
                .append(VERSION, appliedMigration.getVersion().toString())
                .append(DESCRIPTION, appliedMigration.getDescription())
                .append(EXECUTED_AT, Date.from(appliedMigration.getExecutedAt().toInstant()))
        );
    }

    @Override
    public void removeAppliedMigration(AppliedMigration appliedMigration) {
        dbTemplate.delete(configuration.getCollection(), eq(VERSION, appliedMigration.getVersion().toString()));
    }
}
