package db.migration;

import com.mongodb.client.MongoCollection;
import com.monjam.core.api.Context;
import com.monjam.core.api.Migration;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

public class V1_1_0__Change_user_prefix_type implements Migration {
    @Override
    public void up(Context context) {
        MongoCollection collection = collection(context);
        collection.updateMany(context.getSession(), eq("prefix", "Mr."), set("prefix", 1));
        collection.updateMany(context.getSession(), eq("prefix", "Mrs."), set("prefix", 2));
    }

    @Override
    public void down(Context context) {
        MongoCollection collection = collection(context);
        collection.updateMany(context.getSession(), eq("prefix", 1), set("prefix", "Mr."));
        collection.updateMany(context.getSession(), eq("prefix", 2), set("prefix", "Mrs."));
    }

    private MongoCollection collection(Context context) {
        return context.getDatabase().getCollection("users");
    }
}
