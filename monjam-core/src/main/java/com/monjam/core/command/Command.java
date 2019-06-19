package com.monjam.core.command;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.monjam.core.api.Configuration;
import com.monjam.core.api.MonJamException;
import com.monjam.core.history.DbMigrationHistory;
import com.monjam.core.history.MigrationHistory;
import com.monjam.core.resolve.JavaMigrationResolver;
import com.monjam.core.resolve.MigrationResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Command {
    private static final Logger LOG = LoggerFactory.getLogger(Command.class);

    protected Configuration configuration;

    public Command(Configuration configuration) {
        this.configuration = configuration;
    }

    public void execute() {
        try (MongoClient mongoClient = createDbConnection()) {
            MongoDatabase database = mongoClient.getDatabase(configuration.getDatabase());

            MigrationHistory migrationHistory = new DbMigrationHistory(database, configuration);
            MigrationResolver migrationResolver = new JavaMigrationResolver(configuration);

            doExecute(database, migrationResolver, migrationHistory);
        } catch (Exception e) {
            LOG.error("Error while executing command", e);
        }
    }

    protected MongoClient createDbConnection() {
        if (configuration.getUrl() == null) {
            throw new MonJamException("Could not find database connection url");
        }

        return MongoClients.create(MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(configuration.getUrl()))
                .credential(MongoCredential.createCredential(configuration.getUsername(), "admin", configuration.getPassword().toCharArray()))
                .build()
        );
    }

    protected abstract void doExecute(MongoDatabase database, MigrationResolver migrationResolver, MigrationHistory migrationHistory);
}
