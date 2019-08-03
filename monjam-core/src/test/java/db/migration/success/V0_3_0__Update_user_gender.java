package db.migration.success;

import com.monjam.core.api.Context;
import com.monjam.core.api.Migration;
import com.monjam.core.database.DbTemplate;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

public class V0_3_0__Update_user_gender implements Migration {
    private static final String COLLECTION = "users";

    @Override
    public void up(Context context) {
        dbTemplate(context).update(COLLECTION, eq("gender", "M"), set("gender", 1));
        dbTemplate(context).update(COLLECTION, eq("gender", "F"), set("gender", 2));
    }

    @Override
    public void down(Context context) {
        dbTemplate(context).update(COLLECTION, eq("gender", 1), set("gender", "M"));
        dbTemplate(context).update(COLLECTION, eq("gender", 2), set("gender", "F"));
    }

    private DbTemplate dbTemplate(Context context) {
        return new DbTemplate(context.getClient(), context.getSession(), context.getConfiguration().getDatabase());
    }
}
