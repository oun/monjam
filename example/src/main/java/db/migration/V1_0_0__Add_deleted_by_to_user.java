package db.migration;

import com.mongodb.client.MongoCollection;
import com.monjam.core.api.Context;
import com.monjam.core.api.Migration;

import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Updates.unset;

public class V1_0_0__Add_deleted_by_to_user implements Migration {
    @Override
    public void up(Context context) {
        collection(context).updateMany(context.getSession(), exists("deletedBy", false), set("deletedBy", null));
    }

    @Override
    public void down(Context context) {
        collection(context).updateMany(context.getSession(), exists("deletedBy", true), unset("deletedBy"));
    }

    private MongoCollection collection(Context context) {
        return context.getDatabase().getCollection("users");
    }
}
