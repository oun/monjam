package com.monjam.core.database;

import com.mongodb.client.FindIterable;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;

public interface DbCollection {
    FindIterable find(Bson sort);

    FindIterable find(Bson filter, Bson sort);

    UpdateResult updateOne(Bson filter, Bson update);

    UpdateResult updateMany(Bson filter, Bson update);

    void insertOne(Document document);

    DeleteResult deleteOne(Bson filter);

    DeleteResult deleteMany(Bson filter);

    void createIndex(Bson keys);

    void dropIndex(Bson keys);
}
