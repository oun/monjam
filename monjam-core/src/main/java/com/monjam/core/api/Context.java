package com.monjam.core.api;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoDatabase;

public class Context {
    private MongoDatabase database;
    private ClientSession session;

    public Context(MongoDatabase database, ClientSession session) {
        this.database = database;
        this.session = session;
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public ClientSession getSession() {
        return session;
    }
}
