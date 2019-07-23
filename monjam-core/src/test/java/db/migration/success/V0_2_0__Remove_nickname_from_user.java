package db.migration.success;

import com.monjam.core.api.Context;
import com.monjam.core.api.Migration;
import com.monjam.core.database.DbTemplate;

import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Updates.unset;

public class V0_2_0__Remove_nickname_from_user implements Migration {
    private static final String COLLECTION = "users";

    @Override
    public void up(Context context) {
        dbTemplate(context).update(COLLECTION, exists("nickname", true), unset("nickname"));
    }

    @Override
    public void down(Context context) {
    }

    private DbTemplate dbTemplate(Context context) {
        return new DbTemplate(context.getClient(), context.getSession(), context.getConfiguration().getDatabase());
    }
}
