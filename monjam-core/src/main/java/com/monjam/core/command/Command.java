package com.monjam.core.command;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.monjam.core.configuration.Configuration;
import com.monjam.core.api.Context;
import com.monjam.core.api.MonJamException;
import com.monjam.core.database.DbTemplate;
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
        try (MongoClient client = createDbConnection(); ClientSession session = startSession(client)) {
            boolean supportTransaction = session != null;
            DbTemplate dbTemplate = new DbTemplate(client, session, configuration.getDatabase());

            MigrationHistory migrationHistory = new DbMigrationHistory(dbTemplate, configuration);
            MigrationResolver migrationResolver = new JavaMigrationResolver(configuration);

            MongoDatabase database = client.getDatabase(configuration.getDatabase());
            Context context = new Context(client, database, session, configuration, supportTransaction);
            doExecute(context, migrationResolver, migrationHistory);
        } catch (Exception e) {
            LOG.error("Error while executing command", e);
        }
    }

    private ClientSession startSession(MongoClient client) {
        try {
            return client.startSession();
        } catch (Exception e) {
            LOG.info("Mongo connection does not support transaction", e);
            return null;
        }
    }

    protected MongoClient createDbConnection() {
        if (configuration.getUrl() == null) {
            throw new MonJamException("Could not find database connection url");
        }

        MongoClientSettings.Builder clientSettingsBuilder = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(configuration.getUrl()));
        if (configuration.getUsername() != null) {
            clientSettingsBuilder.credential(MongoCredential.createCredential(configuration.getUsername(), configuration.getAuthDatabase(), configuration.getPassword().toCharArray()));
        }

        return MongoClients.create(clientSettingsBuilder.build());
    }

    protected abstract void doExecute(Context context, MigrationResolver migrationResolver, MigrationHistory migrationHistory);
}
