package db.migration;

import com.mongodb.client.MongoCollection;
import com.monjam.core.api.Context;
import com.monjam.core.api.Migration;

import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Updates.unset;

public class V1_2_0__Remove_age_from_user implements Migration {
    @Override
    public void up(Context context) {
        collection(context).updateMany(context.getSession(), exists("age"), unset("age"));
    }

    @Override
    public void down(Context context) {
    }

    private MongoCollection collection(Context context) {
        return context.getDatabase().getCollection("users");
    }
}
