package db.migration.success;

import com.monjam.core.api.Context;
import com.monjam.core.api.Migration;
import com.monjam.core.database.DbTemplate;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Updates.unset;

public class V0_1_0__Add_prefix_to_user implements Migration {
    private static final String COLLECTION = "users";

    @Override
    public void up(Context context) {
        dbTemplate(context).update(COLLECTION, eq("gender", "M"), set("prefix", "Mr."));
        dbTemplate(context).update(COLLECTION, eq("gender", "F"), set("prefix", "Mrs."));
    }

    @Override
    public void down(Context context) {
        dbTemplate(context).update(COLLECTION, exists("prefix", true), unset("prefix"));
    }

    private DbTemplate dbTemplate(Context context) {
        return new DbTemplate(context.getClient(), context.getSession(), context.getConfiguration().getDatabase());
    }
}
