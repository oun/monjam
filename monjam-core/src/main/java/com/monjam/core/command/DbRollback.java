package com.monjam.core.command;

import com.mongodb.client.MongoDatabase;
import com.monjam.core.api.Configuration;
import com.monjam.core.history.MigrationHistory;
import com.monjam.core.resolve.MigrationResolver;

public class DbRollback extends Command {
    public DbRollback(Configuration configuration) {
        super(configuration);
    }

    @Override
    protected void doExecute(MongoDatabase database, MigrationResolver migrationResolver, MigrationHistory migrationHistory) {

    }
}