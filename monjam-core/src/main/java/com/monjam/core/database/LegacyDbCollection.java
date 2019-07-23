package com.monjam.core.database;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;

public class LegacyDbCollection implements DbCollection {
    private final MongoCollection delegate;

    public LegacyDbCollection(MongoDatabase database, String collection) {
        this.delegate = database.getCollection(collection);
    }

    @Override
    public FindIterable find(Bson sort) {
        return delegate.find().sort(sort);
    }

    @Override
    public FindIterable find(Bson filter, Bson sort) {
        return delegate.find(filter).sort(sort);
    }

    @Override
    public UpdateResult updateOne(Bson filter, Bson update) {
        return delegate.updateOne(filter, update);
    }

    @Override
    public UpdateResult updateMany(Bson filter, Bson update) {
        return delegate.updateMany(filter, update);
    }

    @Override
    public void insertOne(Document document) {
        delegate.insertOne(document);
    }

    @Override
    public DeleteResult deleteOne(Bson filter) {
        return delegate.deleteOne(filter);
    }

    @Override
    public DeleteResult deleteMany(Bson filter) {
        return delegate.deleteMany(filter);
    }
}
