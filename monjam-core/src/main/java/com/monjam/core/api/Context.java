package com.monjam.core.api;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.monjam.core.configuration.Configuration;

public class Context {
    private MongoClient client;
    private MongoDatabase database;
    private ClientSession session;
    private Configuration configuration;
    private MigrationType migrationType;
    private boolean supportTransaction;

    public Context() {
    }

    public Context(
            MongoClient client,
            MongoDatabase database,
            ClientSession session,
            Configuration configuration,
            boolean supportTransaction
    ) {
        this.client = client;
        this.database = database;
        this.session = session;
        this.configuration = configuration;
        this.supportTransaction = supportTransaction;
    }

    public MongoClient getClient() {
        return client;
    }

    public void setClient(MongoClient client) {
        this.client = client;
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public void setDatabase(MongoDatabase database) {
        this.database = database;
    }

    public ClientSession getSession() {
        return session;
    }

    public void setSession(ClientSession session) {
        this.session = session;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public MigrationType getMigrationType() {
        return migrationType;
    }

    public void setMigrationType(MigrationType migrationType) {
        this.migrationType = migrationType;
    }

    public boolean isSupportTransaction() {
        return supportTransaction;
    }

    public void setSupportTransaction(boolean supportTransaction) {
        this.supportTransaction = supportTransaction;
    }
}
