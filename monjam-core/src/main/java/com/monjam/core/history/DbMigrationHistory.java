package com.monjam.core.history;

import com.mongodb.client.model.Sorts;
import com.monjam.core.api.Configuration;
import com.monjam.core.api.MigrationVersion;
import com.monjam.core.database.MongoTemplate;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class DbMigrationHistory implements MigrationHistory {
    private static final Logger LOG = LoggerFactory.getLogger(DbMigrationHistory.class);

    private final MongoTemplate mongoTemplate;
    private final Configuration configuration;

    public DbMigrationHistory(MongoTemplate mongoTemplate, Configuration configuration) {
        this.mongoTemplate = mongoTemplate;
        this.configuration = configuration;
        initialize();
    }

    protected void initialize() {
        LOG.debug("Create collection {} if it does not exists", configuration.getCollection());
        mongoTemplate.createCollectionIfNotExists(configuration.getCollection());
    }

    @Override
    public List<AppliedMigration> getAppliedMigrations() {
        Collection<AppliedMigration> appliedMigrations = mongoTemplate.findAll(Sorts.ascending("executedAt"), configuration.getCollection(), document -> new AppliedMigration(
                new MigrationVersion(document.getString("version")),
                document.getString("description"),
                ZonedDateTime.ofInstant(document.getDate("executedAt").toInstant(), ZoneOffset.UTC.normalized())
        ));
        return new ArrayList<>(appliedMigrations);
    }

    @Override
    public void addAppliedMigration(AppliedMigration appliedMigration) {
        mongoTemplate.insert(appliedMigration, configuration.getCollection(), migration -> new Document()
                .append("version", appliedMigration.getVersion().toString())
                .append("description", appliedMigration.getDescription())
                .append("executedAt", Date.from(appliedMigration.getExecutedAt().toInstant()))
        );
    }
}
