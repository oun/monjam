package com.monjam.core.history;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.monjam.core.api.Configuration;
import com.monjam.core.api.MigrationVersion;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DbMigrationHistory implements MigrationHistory {
    private DBCollection collection;

    public DbMigrationHistory(DB database, Configuration configuration) {
        collection = database.getCollection(configuration.getCollection());
    }

    @Override
    public List<AppliedMigration> getAppliedMigrations() {
        List<AppliedMigration> migrationHistories = new ArrayList<>();
        DBCursor cursor = collection.find(new BasicDBObject());
        while (cursor.hasNext()) {
            DBObject document = cursor.next();
            AppliedMigration appliedMigration = new AppliedMigration(
                    new MigrationVersion((String) document.get("version")),
                    (String) document.get("description"),
                    (LocalDateTime) document.get("executedAt")
            );
            migrationHistories.add(appliedMigration);
        }
        return migrationHistories;
    }
}
