package com.monjam.core.database;

import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;

public class SessionDbCollection implements DbCollection {
    private final MongoCollection delegate;
    private final ClientSession session;

    public SessionDbCollection(MongoDatabase database, ClientSession session, String collection) {
        this.delegate = database.getCollection(collection);
        this.session = session;
    }

    @Override
    public FindIterable find(Bson sort) {
        return delegate.find(session).sort(sort);
    }

    @Override
    public FindIterable find(Bson filter, Bson sort) {
        return delegate.find(session, filter).sort(sort);
    }

    @Override
    public void insertOne(Document document) {
        delegate.insertOne(session, document);
    }

    @Override
    public UpdateResult updateOne(Bson filter, Bson update) {
        return delegate.updateOne(session, filter, update);
    }

    @Override
    public UpdateResult updateMany(Bson filter, Bson update) {
        return delegate.updateMany(session, filter, update);
    }

    @Override
    public DeleteResult deleteOne(Bson filter) {
        return delegate.deleteOne(session, filter);
    }

    @Override
    public DeleteResult deleteMany(Bson filter) {
        return delegate.deleteMany(session, filter);
    }
}
