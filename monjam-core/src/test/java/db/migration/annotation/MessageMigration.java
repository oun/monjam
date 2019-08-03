package db.migration.annotation;

import com.monjam.core.annotation.Migrate;
import com.monjam.core.annotation.MongoMigration;
import com.monjam.core.api.Context;
import com.monjam.core.api.MigrationType;
import com.monjam.core.database.DbTemplate;

import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Updates.unset;

@MongoMigration
public class MessageMigration {
    private static final String COLLECTION_NAME = "messages";

    @Migrate(type = MigrationType.MIGRATE, version = "0.3.2", description = "Add read flag to message")
    public void addReadFlag(Context context) {
        dbTemplate(context).update(COLLECTION_NAME, exists("_id", true), set("read", true));
    }

    @Migrate(type = MigrationType.ROLLBACK, version = "0.3.2", description = "Revert add read flag to message")
    public void revertReadFlag(Context context) {
        dbTemplate(context).update(COLLECTION_NAME, exists("read", true), unset("read"));
    }

    private DbTemplate dbTemplate(Context context) {
        String databaseName = context.getConfiguration().getDatabase();
        return new DbTemplate(context.getClient(), context.getSession(), databaseName);
    }
}
