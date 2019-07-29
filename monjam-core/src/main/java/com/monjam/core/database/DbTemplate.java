package com.monjam.core.database;

import com.mongodb.client.ClientSession;
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

public class DbTemplate {
    private static final Logger LOG = LoggerFactory.getLogger(DbTemplate.class);

    private final MongoDatabase database;
    private final ClientSession session;

    public DbTemplate(MongoClient mongoClient, ClientSession session, String database) {
        this.database = mongoClient.getDatabase(database);
        this.session = session;
    }

    public void createCollectionIfNotExists(String collection) {
        boolean exists = database.listCollectionNames().into(new ArrayList<>()).contains(collection);
        if (!exists) {
            database.createCollection(collection);
        }
    }

    public <T> Collection<T> find(Bson sort, String collectionName, Function<Document, T> mapper) {
        List<T> results = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection(collectionName).find(sort).iterator()) {
            while (cursor.hasNext()) {
                results.add(mapper.apply(cursor.next()));
            }
        }
        return results;
    }

    public <T> void insert(T document, String collectionName, Function<T, Document> mapper) {
        collection(collectionName).insertOne(mapper.apply(document));
    }

    public void update(String collectionName, Bson filter, Bson update) {
        collection(collectionName).updateMany(filter, update);
    }

    public void delete(String collectionName, Bson filter) {
        collection(collectionName).deleteMany(filter);
    }

    public void executeCommand(String command) {
        Document cmd = new Document("eval", command);
        // eval command is not supported in multi-document transaction
        database.runCommand(cmd);
    }

    private DbCollection collection(String collectionName) {
        return session != null
                ? new SessionDbCollection(database, session, collectionName)
                : new LegacyDbCollection(database, collectionName);
    }
}
