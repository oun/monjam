package com.monjam.core.database;

import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;

public class SessionMongoTemplate extends MongoTemplate {
    private final ClientSession session;
    private final MongoDatabase database;

    public SessionMongoTemplate(MongoClient mongoClient, ClientSession session, String database) {
        super(mongoClient, database);
        this.session = session;
        this.database = mongoClient.getDatabase(database);
    }

    @Override
    protected FindIterable doFind(String collection) {
        return database.getCollection(collection).find(session);
    }

    @Override
    protected void doInsertOne(String collection, Document document) {
        database.getCollection(collection).insertOne(session, document);
    }

    @Override
    protected void doDeleteMany(String collection, Bson filter) {
        database.getCollection(collection).deleteMany(session, filter);
    }
}
