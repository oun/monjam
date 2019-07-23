package com.monjam.core.database;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class MongoTemplate {
    private static final Logger LOG = LoggerFactory.getLogger(MongoTemplate.class);

    private final MongoDatabase database;

    public MongoTemplate(MongoClient mongoClient, String database) {
        this.database = mongoClient.getDatabase(database);
    }

    public void createCollectionIfNotExists(String collection) {
        boolean exists = database.listCollectionNames().into(new ArrayList<>()).contains(collection);
        if (!exists) {
            database.createCollection(collection);
        }
    }

    public <T> Collection<T> findAll(Bson sort, String collection, Function<Document, T> mapper) {
        List<T> results = new ArrayList<>();
        try (MongoCursor<Document> cursor = doFind(collection).sort(sort).iterator()) {
            while (cursor.hasNext()) {
                results.add(mapper.apply(cursor.next()));
            }
        }
        return results;
    }

    protected FindIterable doFind(String collection) {
        return database.getCollection(collection).find();
    }

    public <T> void insert(T document, String collection, Function<T, Document> mapper) {
        doInsertOne(collection, mapper.apply(document));
    }

    protected void doInsertOne(String collection, Document document) {
        database.getCollection(collection).insertOne(document);
    }

    public <T> void deleteMany(String collection, Bson filter) {
        doDeleteMany(collection, filter);
    }

    protected void doDeleteMany(String collection, Bson filter) {
        database.getCollection(collection).deleteMany(filter);
    }
}
