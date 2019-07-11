package db.migration.success;

import com.mongodb.client.MongoDatabase;
import com.monjam.core.api.Context;
import com.monjam.core.api.Migration;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Date;

public class V0_1_0__Create_Collection implements Migration {
    private static final String COLLECTION = "messages";

    @Override
    public void up(Context context) {
        MongoDatabase database = context.getDatabase();

        database.createCollection(COLLECTION);

        database.getCollection(COLLECTION).insertOne(context.getSession(), new Document()
                .append("_id", new ObjectId("5d1c7aa40130ca121404a176"))
                .append("message", "Sawasdee Earthling")
                .append("sender", "Alien")
                .append("time", new Date())
        );
    }

    @Override
    public void down(Context context) {
        MongoDatabase database = context.getDatabase();

        database.getCollection(COLLECTION).drop();
    }
}
