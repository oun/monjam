package db.migration.failure;

import com.monjam.core.api.Context;
import com.monjam.core.api.Migration;
import com.monjam.core.database.DbTemplate;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Updates.unset;

public class V0_1_2__Add_date_of_birth_to_user implements Migration {
    private static final String COLLECTION = "messages";

    @Override
    public void up(Context context) {
        dbTemplate(context).update(COLLECTION, eq("age", 30), set("dateOfBirth", "1999-10-27"));

        throw new RuntimeException("Oops! Something went wrong");
    }

    @Override
    public void down(Context context) {
        dbTemplate(context).update(COLLECTION, exists("dateOfBirth", true), unset("dateOfBirth"));
    }

    private DbTemplate dbTemplate(Context context) {
        return new DbTemplate(context.getClient(), context.getSession(), context.getConfiguration().getDatabase());
    }
}
