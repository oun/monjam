package com.monjam.core.history;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import com.monjam.core.api.Configuration;
import com.monjam.core.api.MigrationVersion;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DbMigrationHistory implements MigrationHistory {
    private static final Logger LOG = LoggerFactory.getLogger(DbMigrationHistory.class);

    private MongoCollection<Document> collection;

    public DbMigrationHistory(MongoDatabase database, Configuration configuration) {
        collection = database.getCollection(configuration.getCollection());
    }

    @Override
    public List<AppliedMigration> getAppliedMigrations() {
        List<AppliedMigration> migrationHistories = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection.find().sort(Sorts.ascending("executedAt")).iterator()) {
            while (cursor.hasNext()) {
                Document document = cursor.next();
                AppliedMigration appliedMigration = new AppliedMigration(
                        new MigrationVersion(document.getString("version")),
                        document.getString("description"),
                        ZonedDateTime.ofInstant(document.getDate("executedAt").toInstant(), ZoneOffset.UTC.normalized())
                );
                migrationHistories.add(appliedMigration);
            }
        }
        return migrationHistories;
    }

    @Override
    public void addAppliedMigration(AppliedMigration appliedMigration) {
        Document document = new Document()
                .append("version", appliedMigration.getVersion().toString())
                .append("description", appliedMigration.getDescription())
                .append("executedAt", Date.from(appliedMigration.getExecutedAt().toInstant()));
        collection.insertOne(document);
    }
}
