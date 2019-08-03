package db.migration.annotation;

import com.mongodb.client.model.Indexes;
import com.monjam.core.annotation.Migrate;
import com.monjam.core.annotation.MongoMigration;
import com.monjam.core.api.Context;
import com.monjam.core.api.MigrationType;
import com.monjam.core.database.DbTemplate;

import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Updates.unset;

@MongoMigration
public class UserMigration {
    private static final String COLLECTION_NAME = "users";

    @Migrate(type = MigrationType.MIGRATE, version = "0.1.2", description = "Add marital status")
    public void addMaritalStatus(Context context) {
        dbTemplate(context).update(COLLECTION_NAME, exists("_id", true), set("maritalStatus", "M"));
    }

    @Migrate(type = MigrationType.ROLLBACK, version = "0.1.2", description = "Revert add marital status")
    public void revertMaritalStatus(Context context) {
        dbTemplate(context).update(COLLECTION_NAME, exists("maritalStatus", true), unset("maritalStatus"));
    }

    @Migrate(type = MigrationType.MIGRATE, version = "0.2.2", description = "Add username index")
    public void addUsernameIndex(Context context) {
        dbTemplate(context).createIndex(COLLECTION_NAME, Indexes.ascending("username"));
    }

    @Migrate(type = MigrationType.ROLLBACK, version = "0.2.2", description = "Revert add username index")
    public void revertUsernameIndex(Context context) {
        dbTemplate(context).dropIndex(COLLECTION_NAME, Indexes.ascending("username"));
    }

    private DbTemplate dbTemplate(Context context) {
        String databaseName = context.getConfiguration().getDatabase();
        return new DbTemplate(context.getClient(), context.getSession(), databaseName);
    }
}
