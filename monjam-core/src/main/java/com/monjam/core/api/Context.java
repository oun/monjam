package com.monjam.core.api;

import com.mongodb.client.MongoDatabase;

public class Context {
    private MongoDatabase database;

    public Context(MongoDatabase database) {
        this.database = database;
    }

    public MongoDatabase getDatabase() {
        return database;
    }
}
