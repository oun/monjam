package db.migration.failure;

import com.mongodb.client.MongoDatabase;
import com.monjam.core.api.Context;
import com.monjam.core.api.Migration;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Updates.unset;

public class V0_1_1__Add_Field_Failed implements Migration {
    private static final String COLLECTION = "messages";

    @Override
    public void up(Context context) {
        MongoDatabase database = context.getDatabase();

        Bson query = eq("_id", new ObjectId("5d1c7aa40130ca121404a176"));
        if (context.isSupportTransaction()) {
            database.getCollection(COLLECTION).updateOne(context.getSession(), query, set("subject", "Test"));
        } else {
            database.getCollection(COLLECTION).updateOne(query, set("subject", "Test"));
        }

        throw new RuntimeException("Oops! Something went wrong");
    }

    @Override
    public void down(Context context) {
        MongoDatabase database = context.getDatabase();

        Bson query = eq("_id", new ObjectId("5d1c7aa40130ca121404a176"));
        if (context.isSupportTransaction()) {
            database.getCollection(COLLECTION).updateOne(context.getSession(), query, unset("subject"));
        } else {
            database.getCollection(COLLECTION).updateOne(query, unset("subject"));
        }
    }
}
