package com.monjam.core.database;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
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
    private final ClientSession session;

    public MongoTemplate(MongoClient mongoClient, ClientSession session, String database) {
        this.database = mongoClient.getDatabase(database);
        this.session = session;
    }

    public void createCollectionIfNotExists(String collection) {
        boolean exists = database.listCollectionNames().into(new ArrayList<>()).contains(collection);
        if (!exists) {
            database.createCollection(collection);
        }
    }

    public <T> Collection<T> findAll(Bson sort, String collection, Function<Document, T> mapper) {
        List<T> results = new ArrayList<>();
        try (MongoCursor<Document> cursor = getCollection(collection).find(session).sort(sort).iterator()) {
            while (cursor.hasNext()) {
                results.add(mapper.apply(cursor.next()));
            }
        }
        return results;
    }

    public <T> void insert(T document, String collection, Function<T, Document> mapper) {
        getCollection(collection).insertOne(session, mapper.apply(document));
    }

    public <T> void delete(Bson filter, String collection) {
        getCollection(collection).deleteMany(session, filter);
    }

    private MongoCollection<Document> getCollection(String collection) {
        return database.getCollection(collection);
    }
}
