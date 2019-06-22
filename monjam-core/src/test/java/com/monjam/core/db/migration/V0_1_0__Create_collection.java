package com.monjam.core.db.migration;

import com.mongodb.client.MongoDatabase;
import com.monjam.core.api.Context;
import com.monjam.core.api.Migration;
import org.bson.Document;

import java.util.Date;

public class V0_1_0__Create_collection implements Migration {
    private static final String COLLECTION = "messages";

    @Override
    public void up(Context context) {
        MongoDatabase database = context.getDatabase();

        database.createCollection(COLLECTION);

        database.getCollection(COLLECTION).insertOne(new Document()
                .append("message", "Sawasdee Earthling")
                .append("sender", "Alien")
                .append("time", new Date())
        );
    }

    @Override
    public void down(Context context) {

    }
}
