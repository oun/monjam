package com.monjam.core.db.migration;

import com.mongodb.client.MongoDatabase;
import com.monjam.core.api.Context;
import com.monjam.core.api.Migration;

import static com.mongodb.client.model.Indexes.ascending;

public class V0_1_1__Create_index implements Migration {
    private static final String COLLECTION = "messages";

    @Override
    public void up(Context context) {
        MongoDatabase database = context.getDatabase();

        database.getCollection(COLLECTION).createIndex(ascending("time"));
    }

    @Override
    public void down(Context context) {

    }
}
