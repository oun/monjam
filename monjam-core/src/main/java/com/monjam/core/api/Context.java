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
    private boolean supportTransaction;

    public Context(MongoClient client, MongoDatabase database, ClientSession session, Configuration configuration, boolean supportTransaction) {
        this.client = client;
        this.database = database;
        this.session = session;
        this.configuration = configuration;
        this.supportTransaction = supportTransaction;
    }

    public MongoClient getClient() {
        return client;
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public ClientSession getSession() {
        return session;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public boolean isSupportTransaction() {
        return supportTransaction;
    }
}
